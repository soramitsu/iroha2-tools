package jp.co.genesis.listener

import jp.co.genesis.config.Iroha2Config
import jp.co.genesis.service.IrohaService
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class ContextListener(
    val config: Iroha2Config,
    val irohaService: IrohaService
) : ApplicationListener<ApplicationReadyEvent> {

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        try {
            println(irohaService.query(config))
        } catch (e: Exception) {
            println("Failed to query Iroha2: ${e.message}")
        }
        event.applicationContext.close()
    }
}
