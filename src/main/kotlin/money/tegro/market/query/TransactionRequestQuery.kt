package money.tegro.market.query

import com.expediagroup.graphql.generator.annotations.GraphQLName
import money.tegro.market.toBase64
import org.ton.bigint.BigInt
import org.ton.block.AddrStd
import org.ton.block.MsgAddressInt
import org.ton.cell.Cell

@GraphQLName("TransactionRequest")
data class TransactionRequestQuery(
    val dest: String,
    val value: String,
    val stateInit: String?,
    val text: String?,
    val payload: String?,
) {
    constructor(dest: MsgAddressInt, value: BigInt, stateInit: Cell?, text: String?, payload: Cell?) : this(
        dest = (dest as AddrStd).toString(userFriendly = true, urlSafe = true),
        value = value.toString(),
        stateInit = stateInit?.toBase64(),
        text = text,
        payload = payload?.toBase64(),
    )
}
