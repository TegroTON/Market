package money.tegro.market.nft_tool

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import io.ipfs.api.IPFS
import kotlinx.coroutines.runBlocking
import money.tegro.market.nft.NFTCollection
import money.tegro.market.nft.NFTContent
import money.tegro.market.nft.NFTContentOffChain
import money.tegro.market.nft.NFTItem
import mu.KLogging
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.ton.block.MsgAddressInt
import org.ton.crypto.hex
import org.ton.lite.api.LiteApi

class LiteClientOptions : OptionGroup("lite-client options") {
    val host by option("-o", "--host", help = "Lite server host IP address")
        .default("5.9.10.47")
    val port by option("-p", "--port", help = "Lite server port number").int().default(19949)
    val publicKey by option("-k", "--pubkey", help = "Lite server public key")
        .default("9f85439d2094b92a639c2c9493d7b740e39dea8d08b525986d39d6dd69e7f309")
}

class Tool : CliktCommand(name = "nft_tool", help = ""), KoinComponent {
    private val liteClientOptions by LiteClientOptions()
    private val ipfsAddress by option("-i", "--ipfs", help = "Address of the IPFS API server")
        .default("/ip4/127.0.0.1/tcp/5001")
    private val verbose by option("-v", "--verbose", help = "Verbose output").int().default(0)
    private val retries by option(
        "-r",
        "--retry",
        help = "Retry N times before giving up. 0 for infinite retries"
    ).int()
        .default(1)

    override fun run() {
        runBlocking {
            when (verbose) {
                0 -> {
                    getKoin().logger.level = Level.ERROR
                    (KotlinLogging.logger(org.slf4j.Logger.ROOT_LOGGER_NAME).underlyingLogger as ch.qos.logback.classic.Logger).level =
                        ch.qos.logback.classic.Level.WARN
                }
                1 -> {
                    getKoin().logger.level = Level.INFO
                    (KotlinLogging.logger(org.slf4j.Logger.ROOT_LOGGER_NAME).underlyingLogger as ch.qos.logback.classic.Logger).level =
                        ch.qos.logback.classic.Level.INFO
                }
                2 -> {
                    getKoin().logger.level = Level.DEBUG
                    (KotlinLogging.logger(org.slf4j.Logger.ROOT_LOGGER_NAME).underlyingLogger as ch.qos.logback.classic.Logger).level =
                        ch.qos.logback.classic.Level.DEBUG
                }
                else ->
                    logger.warn("Verbose level $verbose is not valid, ignoring")
            }

            val liteClient: LiteApi by inject {
                parametersOf(liteClientOptions.host, liteClientOptions.port, hex(liteClientOptions.publicKey), retries)
            }

            logger.debug("connecting to the lite client at ${liteClientOptions.host}:${liteClientOptions.port}")
            (liteClient as ResilientLiteClient).connect()

            val ipfs: IPFS by inject {
                parametersOf(ipfsAddress)
            }

            logger.debug("ipfs api ${ipfs.version()} is initialized")
        }
    }

    companion object : KLogging()
}

class QueryItem : CliktCommand(name = "query-item", help = "Query NFT item info"), KoinComponent {
    val address by argument(name = "address", help = "NFT item contract address")
    override fun run() {
        runBlocking {
            val item = NFTItem.fetch(MsgAddressInt.AddrStd.parse(address))
            println("NFT Item ${item.address.toString(userFriendly = true)}:")
            println("\tInitialized: ${item.initialized}")
            println("\tIndex: ${item.index}")
            println("\tCollection Address: ${item.collection?.toString(userFriendly = true)}")
            println("\tOwner Address: ${item.owner.toString(userFriendly = true)}")

            val fullContent = item.collection?.let {
                NFTCollection.getItemContent(
                    it,
                    item.index,
                    item.content
                )
            }

            val content = NFTContent.parse(fullContent ?: item.content)

            when (content) {
                is NFTContentOffChain -> {
                    println("\tContent URL: ${content.url}")
                }
            }
            println("\tName: ${content.name}")
            println("\tDescription: ${content.description}")
            println("\tImage: ${content.image}")
        }
    }
}

class QueryCollection : CliktCommand(name = "query-collection", help = "Query NFT collection info"), KoinComponent {
    val address by argument(name = "address", help = "NFT collection contract address")

    override fun run() {
        runBlocking {
            val collection = NFTCollection.fetch(MsgAddressInt.AddrStd.parse(address))
            println("NFT Collection ${collection.address.toString(userFriendly = true)}")
            println("\tNumber of items: ${collection.size}")
            println("\tOwner address: ${collection.owner.toString(userFriendly = true)}")

            val content = NFTContent.parse(collection.content)
            when (content) {
                is NFTContentOffChain -> {
                    println("\tContent URL: ${content.url}")
                }
            }
            println("\tName: ${content.name}")
            println("\tDescription: ${content.description}")
            println("\tImage: ${content.image}")

            val royalties = collection.getRoyaltyParameters()
            if (royalties != null) {
                println("\tRoyalty percentage: ${royalties.first * 100f}%")
                println("\tRoyalty destination: ${(royalties.second as MsgAddressInt.AddrStd).toString(userFriendly = true)}")
            }
        }
    }
}

class ListCollection : CliktCommand(name = "list-collection", help = "List all items of the given NFT collection"),
    KoinComponent {
    val address by argument(name = "address", help = "NFT collection contract address")

    override fun run() {
        runBlocking {
            val collection = NFTCollection.fetch(MsgAddressInt.AddrStd.parse(address))

            println("index | address | initialized | owner")

            for (i in 0..collection.size - 1) {
                val item = collection.getItem(i)
                println(
                    "${item.index} | ${item.address.toString(userFriendly = true)} | ${item.initialized} | ${
                        item.owner.toString(
                            userFriendly = true
                        )
                    }"
                )
            }
        }
    }
}

class KoinLogger : Logger(Level.NONE) {
    override fun log(level: Level, msg: MESSAGE) {
        when (level) {
            Level.DEBUG -> logger.debug(msg)
            Level.INFO -> logger.info(msg)
            Level.ERROR -> logger.error(msg)
            Level.NONE -> {}
        }
    }

    companion object : KLogging()
}

suspend fun main(args: Array<String>) {
    startKoin {
        logger(KoinLogger())

        modules(module {
            single { params ->
                ResilientLiteClient(params.get(), params.get(), params.get(), params.get()) as LiteApi
            }
            single { (addr: String) ->
                IPFS(addr)
            }
        })
    }

    Tool().subcommands(QueryItem(), QueryCollection(), ListCollection()).main(args)
}
