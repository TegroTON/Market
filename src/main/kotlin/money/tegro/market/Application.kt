package money.tegro.market

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import reactor.core.scheduler.Schedulers

@OpenAPIDefinition(
    info = Info(
        title = "Market API",
        version = "0.0.1"
    )
)
object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        Schedulers.enableMetrics()

        Micronaut.build(*args)
            .banner(false)
            .start()
    }
}
