package money.tegro.market

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info

@OpenAPIDefinition(
    info = Info(
        title = "Market API",
        version = "0.0.1"
    )
)
object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build(*args)
            .banner(false)
            .start()
    }
}
