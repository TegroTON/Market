package money.tegro.market.nft_tool

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import kotlinx.coroutines.runBlocking
import org.ton.block.MsgAddressInt
import org.ton.crypto.hex
import org.ton.lite.client.LiteClient

suspend fun main(args: Array<String>) {
    var parser = ArgParser("nft_tool")

    val liteServerHost by parser.option(ArgType.String, "host", "o ", "Lite server host IP address")
        .default("67.207.74.182")
    val liteServerPort by parser.option(ArgType.Int, "port", "p", "Lite server port number").default(4924)
    val liteServerPubKey by parser.option(ArgType.String, "pubkey", "k", "Lite server public key")
        .default("a5e253c3f6ab9517ecb204ee7fd04cca9273a8e8bb49712a48f496884c365353")

    class QueryItem : Subcommand("query-item", "Query NFT item info") {
        val address by argument(ArgType.String, "address", "NFT item contract address")

        override fun execute() = runBlocking {
            val liteClient = LiteClient(liteServerHost, liteServerPort, hex(liteServerPubKey)).connect()

            val item = NFTItem.fetch(liteClient, MsgAddressInt.AddrStd.parse(address))
            println("NFT Item ${item.address.toString(userFriendly = true)}:")
            println("\tInitialized: ${item.initialized}")
            println("\tIndex: ${item.index}")
            println("\tCollection Address: ${item.collection?.address?.toString(userFriendly = true)}")
            println("\tOwner Address: ${item.owner.toString(userFriendly = true)}")

            if (item.collection != null) {
                println("NFT Collection ${item.collection.address.toString(userFriendly = true)}")
                println("\tNext item index: ${item.collection.nextItemIndex}")
                println("\tOwner Address: ${item.collection.owner.toString(userFriendly = true)}")
            }
        }
    }

    val queryItem = QueryItem()

    parser.subcommands(queryItem)

    parser.parse(args)
}
