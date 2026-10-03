package ingsis.snippet.permission.controller

import ingsis.snippet.permission.config.SecurityConfig
import ingsis.snippet.permission.controller.dto.RulesResponse
import ingsis.snippet.permission.service.RulesService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

@WebMvcTest(InternalRulesController::class)
@Import(SecurityConfig::class)
class InternalRulesControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var rulesService: RulesService

    @Test
    fun shouldReturnLintRulesOfAnExplicitUser() {
        val response =
            RulesResponse(
                userId = "auth0|owner",
                rulesVersion = 4,
                rules = mapOf("identifier_format" to "camelCase"),
                updatedAt = Instant.now(),
            )
        whenever(rulesService.getLintRules("auth0|owner")).thenReturn(response)

        mockMvc
            .perform(
                get("/internal/rules/lint/{userId}", "auth0|owner")
                    .with(jwt()),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.rulesVersion").value(4))
            .andExpect(jsonPath("$.rules.identifier_format").value("camelCase"))
    }

    @Test
    fun shouldReturnFormatRulesOfAnExplicitUser() {
        val response =
            RulesResponse(
                userId = "auth0|owner",
                rulesVersion = 2,
                rules = mapOf("space-before-colon" to true),
                updatedAt = Instant.now(),
            )
        whenever(rulesService.getFormatRules("auth0|owner")).thenReturn(response)

        mockMvc
            .perform(
                get("/internal/rules/format/{userId}", "auth0|owner")
                    .with(jwt()),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.rulesVersion").value(2))
            .andExpect(jsonPath("$.rules.space-before-colon").value(true))
    }
}
