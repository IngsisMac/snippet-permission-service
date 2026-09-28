package ingsis.snippet.permission.service

import com.fasterxml.jackson.databind.ObjectMapper
import ingsis.snippet.permission.controller.dto.UpdateRulesRequest
import ingsis.snippet.permission.domain.model.FormatConfig
import ingsis.snippet.permission.domain.model.LintConfig
import ingsis.snippet.permission.domain.repository.FormatConfigRepository
import ingsis.snippet.permission.domain.repository.LintConfigRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

class RulesServiceTest {
    private lateinit var lintConfigRepository: LintConfigRepository
    private lateinit var formatConfigRepository: FormatConfigRepository
    private lateinit var objectMapper: ObjectMapper
    private lateinit var rulesService: RulesService

    @BeforeEach
    fun setUp() {
        lintConfigRepository = mock()
        formatConfigRepository = mock()
        objectMapper = ObjectMapper()
        rulesService = RulesService(lintConfigRepository, formatConfigRepository, objectMapper)
    }

    @Test
    fun shouldGetDefaultLintRulesWhenNonePersisted() {
        val userId = "auth0|user123"
        whenever(lintConfigRepository.findById(userId)).thenReturn(Optional.empty())

        val response = rulesService.getLintRules(userId)

        assertNotNull(response)
        assertEquals(userId, response.userId)
        assertEquals(1, response.rulesVersion)
    }

    @Test
    fun shouldUpdateLintRulesAndIncrementVersion() {
        val userId = "auth0|user123"
        val existing = LintConfig(userId = userId, rulesVersion = 1, rules = "{}")
        val request = UpdateRulesRequest(rules = mapOf("identifier_format" to "camelCase"))

        whenever(lintConfigRepository.findById(userId)).thenReturn(Optional.of(existing))
        whenever(lintConfigRepository.save(any<LintConfig>())).thenAnswer { it.arguments[0] }

        val response = rulesService.updateLintRules(userId, request)

        assertEquals(2, response.rulesVersion)
        assertEquals("camelCase", response.rules["identifier_format"])
        verify(lintConfigRepository).save(any<LintConfig>())
    }

    @Test
    fun shouldGetDefaultFormatRulesWhenNonePersisted() {
        val userId = "auth0|user123"
        whenever(formatConfigRepository.findById(userId)).thenReturn(Optional.empty())

        val response = rulesService.getFormatRules(userId)

        assertNotNull(response)
        assertEquals(userId, response.userId)
        assertEquals(1, response.rulesVersion)
    }

    @Test
    fun shouldUpdateFormatRulesAndIncrementVersion() {
        val userId = "auth0|user123"
        val existing = FormatConfig(userId = userId, rulesVersion = 3, rules = "{}")
        val request = UpdateRulesRequest(rules = mapOf("space-before-colon" to true))

        whenever(formatConfigRepository.findById(userId)).thenReturn(Optional.of(existing))
        whenever(formatConfigRepository.save(any<FormatConfig>())).thenAnswer { it.arguments[0] }

        val response = rulesService.updateFormatRules(userId, request)

        assertEquals(4, response.rulesVersion)
        assertEquals(true, response.rules["space-before-colon"])
        verify(formatConfigRepository).save(any<FormatConfig>())
    }
}
