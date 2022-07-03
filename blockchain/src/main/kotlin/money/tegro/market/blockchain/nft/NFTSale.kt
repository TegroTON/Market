package money.tegro.market.blockchain.nft

import mu.KLogging
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.block.AddrStd
import org.ton.block.MsgAddress
import org.ton.block.VmStackValue
import org.ton.lite.api.LiteApi
import org.ton.lite.api.liteserver.LiteServerAccountId
import org.ton.tlb.loadTlb

interface NFTSale : Addressable {
    val marketplace: MsgAddress
    val item: MsgAddress
    val owner: MsgAddress
    val price: Long
    val marketplaceFee: Long
    val royaltyDestination: MsgAddress
    val royalty: Long

    companion object : KLogging() {
        private val msgAddressCodec by lazy { MsgAddress.tlbCodec() }

        @JvmStatic
        suspend fun of(
            address: AddrStd,
            liteApi: LiteApi,
            referenceBlock: suspend () -> TonNodeBlockIdExt = { liteApi.getMasterchainInfo().last },
        ): NFTSale =
            liteApi.runSmcMethod(0b100, referenceBlock(), LiteServerAccountId(address), "get_sale_data").let {
                require(it.exitCode == 0) { "Failed to run the method, exit code is ${it.exitCode}" }

                NFTSaleImpl(
                    address,
                    (it[0] as VmStackValue.Slice).toCellSlice().loadTlb(msgAddressCodec),
                    (it[1] as VmStackValue.Slice).toCellSlice().loadTlb(msgAddressCodec),
                    (it[2] as VmStackValue.Slice).toCellSlice().loadTlb(msgAddressCodec),
                    (it[3] as VmStackValue.TinyInt).value,
                    (it[4] as VmStackValue.TinyInt).value,
                    (it[5] as VmStackValue.Slice).toCellSlice().loadTlb(msgAddressCodec),
                    (it[6] as VmStackValue.TinyInt).value,
                )
            }
    }
}

private data class NFTSaleImpl(
    override val address: MsgAddress,
    override val marketplace: MsgAddress,
    override val item: MsgAddress,
    override val owner: MsgAddress,
    override val price: Long,
    override val marketplaceFee: Long,
    override val royaltyDestination: MsgAddress,
    override val royalty: Long
) : NFTSale
