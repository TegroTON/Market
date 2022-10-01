package money.tegro.market.model

import money.tegro.market.contract.nft.ItemContract
import money.tegro.market.contract.nft.RoyaltyContract
import money.tegro.market.contract.nft.SaleContract
import money.tegro.market.metadata.ItemMetadata
import money.tegro.market.properties.MarketplaceProperties
import mu.KLogging
import org.ton.bigint.BigInt
import org.ton.block.AddrNone
import org.ton.block.MsgAddress
import org.ton.block.MsgAddressInt

data class ItemModel(
    val address: MsgAddressInt,
    val index: ULong,
    val collection: MsgAddress,
    val owner: MsgAddress,
    val name: String,
    val description: String,
    val image: String,
    val attributes: Map<String, String>,
    val sale: MsgAddress,
    val marketplace: MsgAddress,
    val fullPrice: BigInt?,
    val marketplaceFee: BigInt?,
    val royalties: BigInt?,
    val royaltyDestination: MsgAddress,
    val royaltyNumerator: Int,
    val royaltyDenominator: Int,
    val marketplaceFeeNumerator: Int,
    val marketplaceFeeDenominator: Int,
    val saleInitializationFee: BigInt,
    val transferFee: BigInt,
    val networkFee: BigInt,
    val minimalGasFee: BigInt,
) {
    companion object : KLogging() {
        @JvmStatic
        fun of(
            address: MsgAddressInt,
            contract: ItemContract?,
            metadata: ItemMetadata?,
            royalty: RoyaltyContract?,
            sale: SaleContract?,
            properties: MarketplaceProperties,
        ): ItemModel {
            val index = contract?.index ?: 0uL

            return ItemModel(
                address = address,
                index = index,
                collection = contract?.collection ?: AddrNone,
                owner = sale?.owner ?: (contract?.owner ?: AddrNone),
                name = metadata?.name ?: "Item no. $index",
                description = metadata?.description.orEmpty(),
                image = metadata?.image ?: "", // TODO
                attributes = metadata?.attributes.orEmpty().associate { it.trait to it.value },
                sale = if (sale != null) contract?.owner ?: AddrNone else AddrNone,
                marketplace = sale?.marketplace ?: AddrNone,
                fullPrice = sale?.full_price,
                marketplaceFee = sale?.marketplace_fee,
                royalties = sale?.royalty,
                royaltyDestination = sale?.royalty_destination ?: royalty?.destination ?: AddrNone,
                royaltyNumerator = royalty?.numerator ?: 0,
                royaltyDenominator = royalty?.denominator ?: 1,
                marketplaceFeeNumerator = properties.marketplaceFeeNumerator,
                marketplaceFeeDenominator = properties.marketplaceFeeDenominator,
                saleInitializationFee = properties.saleInitializationFee,
                transferFee = properties.itemTransferFee,
                networkFee = properties.networkFee,
                minimalGasFee = properties.minimalGasFee,
            )
        }
    }
}
