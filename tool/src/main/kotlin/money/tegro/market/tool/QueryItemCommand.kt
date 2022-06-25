package money.tegro.market.tool

import jakarta.inject.Inject
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import money.tegro.market.blockchain.client.ResilientLiteClient
import money.tegro.market.blockchain.nft.NFTDeployedCollectionItem
import money.tegro.market.blockchain.nft.NFTItem
import money.tegro.market.blockchain.nft.NFTMetadata
import money.tegro.market.blockchain.nft.NFTRoyalty
import money.tegro.market.core.dto.ItemDTO
import money.tegro.market.core.dto.RoyaltyDTO
import money.tegro.market.core.dto.toSafeBounceable
import org.ton.block.AddrStd
import org.ton.lite.api.LiteApi
import picocli.CommandLine
import picocli.CommandLine.Parameters

@CommandLine.Command(name = "query-item", description = ["Query api/blockchain for the information about NFT items"])
class QueryItemCommand : Runnable {
    @Inject
    private lateinit var liteApi: LiteApi

    @Inject
    private lateinit var client: ItemClient

    @Parameters(description = ["Addresses of the items to query"])
    private lateinit var addresses: List<String>

    override fun run() {
        runBlocking {
            (liteApi as ResilientLiteClient).connect()
            for (addressStr in addresses) {
                val address = AddrStd(addressStr)
                println("Querying an NFT item ${address.toString(userFriendly = true)}")
                client.getItem(addressStr).awaitSingleOrNull()?.let {
                    println("Found in the Market API:")
                    println(ppDataClass(it))
                }
                queryBlockchain(address).awaitSingleOrNull()?.let {
                    println("Blockchain information:")
                    println(ppDataClass(it))
                }
            }
        }
    }

    fun queryBlockchain(address: AddrStd) = mono {
        val item = NFTItem.of(address, liteApi)
        val royalty = (item as? NFTDeployedCollectionItem)?.collection?.let { NFTRoyalty.of(it, liteApi) }
            ?: NFTRoyalty.of(address, liteApi)
        val metadata = item?.content(liteApi)?.let { NFTMetadata.of(it) }

        item?.let {
            ItemDTO(
                address = it.address.toSafeBounceable(),
                index = it.index,
                collection = (it as? NFTDeployedCollectionItem)?.collection?.toSafeBounceable(),
                owner = it.owner.toSafeBounceable(),
                name = metadata?.name,
                description = metadata?.description,
                attributes = metadata?.attributes?.map { it.trait to it.value }?.toMap() ?: mapOf(),
                royalty = royalty?.let {
                    RoyaltyDTO(
                        it.numerator.toFloat() / it.denominator,
                        it.destination.toSafeBounceable()
                    )
                }
            )
        }
    }
}
