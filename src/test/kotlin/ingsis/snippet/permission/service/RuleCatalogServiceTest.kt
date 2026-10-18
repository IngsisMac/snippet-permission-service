package ingsis.snippet.permission.service

import ingsis.snippet.permission.domain.model.RuleCatalogProperties
import ingsis.snippet.permission.domain.model.RuleType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

/**
 * El catálogo es la frontera entre lo que la interfaz manda y lo que el runner entiende: una
 * clave desconocida que llegue a persistirse se publica a Redis, el runner la ignora y el usuario
 * ve su regla "activa" sin ningún efecto.
 */
class RuleCatalogServiceTest {
    private lateinit var service: RuleCatalogService

    @BeforeEach
    fun setUp() {
        service = RuleCatalogService(RuleCatalogProperties())
    }

    @Test
    fun shouldExposeEveryLintRuleWithItsTypeAndAcceptedValues() {
        val definitions = service.definitions(RuleType.LINT)

        val identifierFormat = definitions.first { it.name == "identifier_format" }
        assertEquals(listOf("camel case", "snake case"), identifierFormat.allowedValues)
        assertTrue(definitions.any { it.name == "no-unused-variables" })
    }

    @Test
    fun shouldExposeTheFormatRulesWithTheirNumericDefaults() {
        val definitions = service.definitions(RuleType.FORMAT)

        assertEquals(4, definitions.first { it.name == "indent-inside-if" }.defaultValue)
        assertTrue(definitions.any { it.name == "enforce-no-space-before-semicolon" })
    }

    @Test
    fun shouldAcceptAConfigurationThatOnlyUsesKnownRules() {
        val rules = mapOf("identifier_format" to "snake case", "no-empty-println" to true)

        assertEquals(rules, service.validate(RuleType.LINT, rules))
    }

    @Test
    fun shouldAcceptAnEmptyConfiguration() {
        assertEquals(emptyMap<String, Any>(), service.validate(RuleType.FORMAT, emptyMap()))
    }

    @Test
    fun shouldRejectARuleThatDoesNotExist() {
        val exception = rejectionOf(RuleType.FORMAT, mapOf("space-before-colon" to true))

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
        assertTrue(exception.reason!!.contains("Unknown format rule 'space-before-colon'"))
    }

    @Test
    fun shouldRejectARuleThatBelongsToTheOtherCatalog() {
        val exception = rejectionOf(RuleType.LINT, mapOf("indent-inside-if" to 4))

        assertTrue(exception.reason!!.contains("Unknown lint rule 'indent-inside-if'"))
    }

    @Test
    fun shouldRejectABooleanRuleGivenSomethingElse() {
        val exception = rejectionOf(RuleType.LINT, mapOf("no-unused-variables" to "yes"))

        assertTrue(exception.reason!!.contains("expects a boolean"))
    }

    @Test
    fun shouldRejectANumericRuleGivenSomethingElse() {
        val exception = rejectionOf(RuleType.FORMAT, mapOf("indent-inside-if" to "four"))

        assertTrue(exception.reason!!.contains("expects a number"))
    }

    @Test
    fun shouldRejectANegativeIndentation() {
        val exception = rejectionOf(RuleType.FORMAT, mapOf("indent-inside-if" to -1))

        assertTrue(exception.reason!!.contains("non-negative"))
    }

    @Test
    fun shouldRejectAValueOutsideTheAcceptedOnes() {
        val exception = rejectionOf(RuleType.LINT, mapOf("identifier_format" to "camelCase"))

        assertTrue(exception.reason!!.contains("accepts only 'camel case' or 'snake case'"))
    }

    @Test
    fun shouldRejectAnEnumeratedRuleGivenANonString() {
        val exception = rejectionOf(RuleType.LINT, mapOf("identifier_format" to true))

        assertTrue(exception.reason!!.contains("expects a string"))
    }

    private fun rejectionOf(
        type: RuleType,
        rules: Map<String, Any>,
    ): ResponseStatusException = assertThrows<ResponseStatusException> { service.validate(type, rules) }
}
