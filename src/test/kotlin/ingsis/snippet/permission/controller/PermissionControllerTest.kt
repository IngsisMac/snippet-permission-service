package ingsis.snippet.permission.controller

import com.fasterxml.jackson.databind.ObjectMapper
import ingsis.snippet.permission.config.SecurityConfig
import ingsis.snippet.permission.controller.dto.PermissionResponse
import ingsis.snippet.permission.controller.dto.ShareSnippetRequest
import ingsis.snippet.permission.domain.model.PermissionLevel
import ingsis.snippet.permission.service.PermissionService
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.UUID

@WebMvcTest(PermissionController::class)
@Import(SecurityConfig::class)
class PermissionControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var permissionService: PermissionService

    @Test
    fun shouldAssignOwnerEndpoint() {
        val snippetId = UUID.randomUUID()
        val userId = "auth0|owner123"
        val response =
            PermissionResponse(
                snippetId = snippetId,
                userId = userId,
                level = PermissionLevel.OWNER,
                grantedAt = Instant.now(),
            )

        whenever(permissionService.assignOwner(snippetId, userId)).thenReturn(response)

        mockMvc
            .perform(
                post("/api/permissions")
                    .param("snippetId", snippetId.toString())
                    .param("userId", userId)
                    .with(jwt()),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.level").value("OWNER"))
            .andExpect(jsonPath("$.userId").value(userId))
    }

    @Test
    fun shouldGetPermissionEndpoint() {
        val snippetId = UUID.randomUUID()
        whenever(permissionService.getPermission(eq(snippetId), any())).thenReturn(PermissionLevel.OWNER)

        mockMvc
            .perform(
                get("/api/permissions/{snippetId}", snippetId)
                    .with(jwt().jwt { it.subject("auth0|owner123") }),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.level").value("OWNER"))
    }

    @Test
    fun shouldShareSnippetEndpoint() {
        val snippetId = UUID.randomUUID()
        val request =
            ShareSnippetRequest(
                targetUserId = "auth0|user2",
                level = PermissionLevel.WRITE,
            )
        val response =
            PermissionResponse(
                snippetId = snippetId,
                userId = "auth0|user2",
                level = PermissionLevel.WRITE,
                grantedAt = Instant.now(),
            )

        whenever(permissionService.shareSnippet(eq(snippetId), eq("auth0|owner123"), any())).thenReturn(response)

        mockMvc
            .perform(
                post("/api/permissions/{snippetId}/share", snippetId)
                    .with(jwt().jwt { it.subject("auth0|owner123") })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.level").value("WRITE"))
            .andExpect(jsonPath("$.userId").value("auth0|user2"))
    }

    @Test
    fun shouldRevokeShareEndpoint() {
        val snippetId = UUID.randomUUID()
        mockMvc
            .perform(
                delete("/api/permissions/{snippetId}/share/{targetUserId}", snippetId, "auth0|user2")
                    .with(jwt().jwt { it.subject("auth0|owner123") }),
            ).andExpect(status().isNoContent)
    }
}
