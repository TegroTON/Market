package money.tegro.market.drive.model

import org.ton.block.MsgAddressIntStd

fun MsgAddressIntStd.toGoodString() = this.toString(userFriendly = true, urlSafe = true, bounceable = true)
