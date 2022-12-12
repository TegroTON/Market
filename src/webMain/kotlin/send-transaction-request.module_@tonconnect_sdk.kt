@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

import kotlin.js.*
import org.khronos.webgl.*
import org.w3c.dom.*
import org.w3c.dom.events.*
import org.w3c.dom.parsing.*
import org.w3c.dom.svg.*
import org.w3c.dom.url.*
import org.w3c.fetch.*
import org.w3c.files.*
import org.w3c.notifications.*
import org.w3c.performance.*
import org.w3c.workers.*
import org.w3c.xhr.*

external interface `T$4` {
    var address: String
    var amount: String
    var stateInit: String?
        get() = definedExternally
        set(value) = definedExternally
    var payload: String?
        get() = definedExternally
        set(value) = definedExternally
}

external interface SendTransactionRequest {
    var validUntil: Number
    var messages: Array<`T$4`>
}