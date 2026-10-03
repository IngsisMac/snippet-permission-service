package ingsis.snippet.permission.controller

import com.fasterxml.jackson.databind.ObjectMapper
import ingsis.snippet.permission.config.SecurityConfig
import ingsis.snippet.permission.controller.dto.RulesResponse
import ingsis.snippet.permission.controller.dto.UpdateRulesRequest
import ingsis.snippet.permission.service.RulesService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

@WebMvcTest(RulesController::class)
@Import(SecurityConfig::class)
class RulesControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var rulesService: RulesService

    @Test
    fun shouldGetMyLintRules() {
        val response =
            RulesResponse(
                userId = "auth0|user123",
                rulesVersion = 2,
                rules = mapOf("identifier_format" to "snake_case"),
                updatedAt = Instant.now(),
            )

        whenever(rulesService.getLintRules("auth0|user123")).thenReturn(response)

        mockMvc
            .perform(
                get("/api/permissions/rules/lint")
                    .with(jwt().jwt { it.subject("auth0|user123") }),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.rulesVersion").value(2))
            .andExpect(jsonPath("$.rules.identifier_format").value("snake_case"))
    }

    @Test
    fun shouldUpdateMyLintRules() {
        val request = UpdateRulesRequest(rules = mapOf("identifier_format" to "camelCase"))
        val response =
            RulesResponse(
                userId = "auth0|user123",
                rulesVersion = 3,
                rules = mapOf("identifier_format" to "camelCase"),
                updatedAt = Instant.now(),
            )

        whenever(rulesService.updateLintRules(eq("auth0|user123"), any())).thenReturn(response)

        mockMvc
            .perform(
                put("/api/permissions/rules/lint")
                    .with(jwt().jwt { it.subject("auth0|user123") })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.rulesVersion").value(3))
    }

    @Test
    fun shouldGetMyFormatRules() {
        val response =
            RulesResponse(
                userId = "auth0|user123",
                rulesVersion = 1,
                rules = mapOf("space-before-colon" to true),
                updatedAt = Instant.now(),
            )

        whenever(rulesService.getFormatRules("auth0|user123")).thenReturn(response)

        mockMvc
            .perform(
                get("/api/permissions/rules/format")
                    .with(jwt().jwt { it.subject("auth0|user123") }),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.rulesVersion").value(1))
    }

    @Test
    fun shouldUpdateMyFormatRules() {
        val request = UpdateRulesRequest(rules = mapOf("space-before-colon" to false))
        val response =
            RulesResponse(
                userId = "auth0|user123",
                rulesVersion = 2,
                rules = mapOf("space-before-colon" to false),
                updatedAt = Instant.now(),
            )

        whenever(rulesService.updateFormatRules(eq("auth0|user123"), any())).thenReturn(response)

        mockMvc
            .perform(
                put("/api/permissions/rules/format")
                    .with(jwt().jwt { it.subject("auth0|user123") })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.rulesVersion").value(2))
    }

    @Test
    fun shouldNotExposeOtherUsersRulesOnPublicApi() {
        mockMvc
            .perform(
                get("/api/permissions/rules/lint/{userId}", "auth0|victim")
                    .with(jwt().jwt { it.subject("auth0|curious") }),
            ).andExpect(status().isNotFound)
    }
}
