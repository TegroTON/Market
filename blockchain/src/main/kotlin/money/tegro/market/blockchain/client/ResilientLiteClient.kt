package money.tegro.market.blockchain.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import mu.KLogging
import org.ton.adnl.AdnlPublicKey
import org.ton.adnl.AdnlTcpClient
import org.ton.adnl.AdnlTcpClientImpl
import org.ton.lite.client.LiteClient
import org.ton.tl.TlCodec

open class ResilientLiteClient(
    adnlTcpClient: AdnlTcpClient
) : LiteClient(adnlTcpClient) {
    constructor(ipv4: Int, port: Int, publicKey: ByteArray) : this(
        AdnlTcpClientImpl(org.ton.adnl.ipv4(ipv4), port, AdnlPublicKey(publicKey), Dispatchers.Default)
    )

    override suspend fun sendRawQuery(byteArray: ByteArray): ByteArray {
        var attempt = 1
        while (true) {
            try {
                return super.sendRawQuery(byteArray)
            } catch (e: Exception) {
                if (e is ClosedReceiveChannelException) { // Connection dropped?
                    this.connect()
                }

                if (attempt >= 1000) {
                    logger.info { "too many attempts, giving up" }
                    throw e
                }

                logger.debug { "attempt no. $attempt failed, trying again in 100ms" }
                delay(100L)
                attempt += 1
            }
        }
    }

    override suspend fun <Q : Any, A : Any> query(query: Q, queryCodec: TlCodec<Q>, answerCodec: TlCodec<A>): A {
        var attempt = 1
        while (true) {
            try {
                return super.query(query, queryCodec, answerCodec)
            } catch (e: Exception) {
                if (attempt >= 100) {
                    logger.info { "too many attempts, giving up" }
                    throw e
                }

                logger.debug { "attempt no. $attempt failed, trying again in 100ms" }
                delay(100L)
                attempt += 1
            }
        }
    }

    companion object : KLogging()
}

