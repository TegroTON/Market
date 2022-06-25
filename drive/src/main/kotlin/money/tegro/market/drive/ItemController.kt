package money.tegro.market.drive

import io.micronaut.data.model.Pageable
import io.micronaut.http.annotation.Controller
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import money.tegro.market.core.configuration.MarketplaceConfiguration
import money.tegro.market.core.dto.ItemDTO
import money.tegro.market.core.dto.TransactionRequestDTO
import money.tegro.market.core.dto.toSafeBounceable
import money.tegro.market.core.operations.ItemOperations
import money.tegro.market.core.repository.AttributeRepository
import money.tegro.market.core.repository.CollectionRepository
import money.tegro.market.core.repository.ItemRepository
import money.tegro.market.core.repository.findByAddressStd
import org.ton.block.AddrNone
import org.ton.block.AddrStd
import org.ton.block.Coins
import org.ton.block.MsgAddress
import org.ton.boc.BagOfCells
import org.ton.cell.CellBuilder
import org.ton.crypto.base64
import org.ton.tlb.storeTlb
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux

@Controller("/items")
class ItemController(
    private val configuration: MarketplaceConfiguration,

    private val collectionRepository: CollectionRepository,
    private val itemRepository: ItemRepository,
    private val attributeRepository: AttributeRepository,
) : ItemOperations {
    override fun getAll(pageable: Pageable) =
        itemRepository.findAll(pageable)
            .flatMapMany {
                it.toFlux().flatMap { item ->
                    mono {
                        ItemDTO(
                            item,
                            item.collection?.let { collectionRepository.findById(it).awaitSingle() },
                            attributeRepository.findByItem(item.address).collectList().awaitSingle()
                        )
                    }
                }
            }

    override fun getItem(item: String) =
        itemRepository.findByAddressStd(AddrStd(item))
            .flatMap {
                mono {
                    ItemDTO(
                        it,
                        it.collection?.let { collectionRepository.findById(it).awaitSingle() },
                        attributeRepository.findByItem(it.address).collectList().awaitSingle()
                    )
                }
            }

    override fun transferItem(
        item: String,
        to: String,
        response: String?,
    ): Mono<TransactionRequestDTO> = mono {
        TransactionRequestDTO(
            to = AddrStd(item).toSafeBounceable(),
            value = 100_000_000, // 0.1 TON
            stateInit = null,
            payload = CellBuilder.createCell {
                storeUInt(0x5fcc3d14, 32) // OP, transfer
                storeUInt(0, 64) // Query id
                storeTlb(MsgAddress.tlbCodec(), AddrStd(to)) // new owner
                storeTlb(
                    MsgAddress.tlbCodec(),
                    response?.let { AddrStd(it) }
                        ?: AddrNone) // response destination. Rest of coins is sent there
                storeInt(0, 1) // custom_data, unused
                storeTlb(Coins.tlbCodec(), Coins.ofNano(6_900_000)) // 0.0069 because funny, forward amount
                // Extra payload here, unused in this case
            }.let { BagOfCells(it) }.toByteArray().let { base64(it) }
        )
    }

    override fun sellItem(
        item: String,
        from: String,
        price: Long,
    ): Mono<TransactionRequestDTO> = mono {
        val it = itemRepository.findByAddressStd(AddrStd(item)).awaitSingle()
        val royalty = it.collection?.let { collectionRepository.findByAddress(it).awaitSingle().royalty }
            ?: it.royalty

        TransactionRequestDTO(
            to = AddrStd(item).toSafeBounceable(),
            value = configuration.saleInitializationFee + configuration.blockchainFee,
            stateInit = null,
            payload = CellBuilder.createCell {
                storeUInt(0x5fcc3d14, 32) // OP, transfer
                storeUInt(0, 64) // Query id
                storeTlb(
                    MsgAddress.tlbCodec(),
                    configuration.marketplaceAddress
                ) // new owner, item is transferred to the marketplace
                storeTlb(
                    MsgAddress.tlbCodec(),
                    AddrStd(from)
                ) // response destination. Rest of coins is sent back
                storeInt(0, 1) // custom_data, unused
                storeTlb(Coins.tlbCodec(), Coins.ofNano(configuration.saleInitializationFee))
                // Extra payload here, used by the market contract to do its magic
                storeRef {
                    storeTlb(Coins.tlbCodec(), Coins.ofNano(price)) // Pure amount user will receive
                    storeTlb(
                        Coins.tlbCodec(),
                        Coins.ofNano(price * configuration.feeNumerator / configuration.feeDenominator)
                    ) // Amount taken by the marketplace
                    storeTlb(
                        MsgAddress.tlbCodec(),
                        royalty?.destination?.to() ?: AddrNone
                    ) // Royalty destination
                    storeTlb(
                        Coins.tlbCodec(),
                        Coins.ofNano(royalty?.let { price * it.numerator * it.denominator }
                            ?: 0L)) // Optional royalty amount
                }
                // TODO: SEVERE: UNLESS  THIS DATA IS SIGNED AND THEN CHECKED BY THE CONTRACT, IT WOULD BE POSSIBLE
                // FOR A MALICIOUS USER TO PUT UP ITEMS FOR SALE WITH NO MARKETPLACE FEE AND/OR ROYALTY
            }.let { BagOfCells(it) }.toByteArray().let { base64(it) }
        )
    }
}
