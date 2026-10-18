package ingsis.snippet.permission

import ingsis.snippet.permission.domain.model.RuleCatalogProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(RuleCatalogProperties::class)
class SnippetPermissionApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<SnippetPermissionApplication>(*args)
}
