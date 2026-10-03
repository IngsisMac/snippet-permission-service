package ingsis.snippet.permission.controller

import ingsis.snippet.permission.config.SecurityConfig
import ingsis.snippet.permission.controller.dto.PermissionResponse
import ingsis.snippet.permission.domain.model.PermissionLevel
import ingsis.snippet.permission.service.PermissionService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.UUID

@WebMvcTest(InternalPermissionController::class)
@Import(SecurityConfig::class)
class InternalPermissionControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var permissionService: PermissionService

    @Test
    fun shouldAssignOwnerForTheGivenUser() {
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
                post("/internal/permissions")
                    .param("snippetId", snippetId.toString())
                    .param("userId", userId)
                    .with(jwt()),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.level").value("OWNER"))
            .andExpect(jsonPath("$.userId").value(userId))
    }

    @Test
    fun shouldResolvePermissionForAnExplicitUser() {
        val snippetId = UUID.randomUUID()
        whenever(permissionService.getPermission(snippetId, "auth0|someone")).thenReturn(PermissionLevel.WRITE)

        mockMvc
            .perform(
                get("/internal/permissions/{snippetId}", snippetId)
                    .param("user", "auth0|someone")
                    .with(jwt()),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.level").value("WRITE"))
    }

    @Test
    fun shouldRejectPermissionLookupWithoutExplicitUser() {
        mockMvc
            .perform(
                get("/internal/permissions/{snippetId}", UUID.randomUUID())
                    .with(jwt()),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun shouldListPermissionsOfAnExplicitUser() {
        val userId = "auth0|user1"
        val snippetId = UUID.randomUUID()
        val permissions =
            listOf(
                PermissionResponse(
                    snippetId = snippetId,
                    userId = userId,
                    level = PermissionLevel.READ,
                    grantedAt = Instant.now(),
                ),
            )
        whenever(permissionService.getUserPermissions(userId, PermissionLevel.READ)).thenReturn(permissions)

        mockMvc
            .perform(
                get("/internal/permissions/user/{userId}", userId)
                    .param("level", "READ")
                    .with(jwt()),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$[0].snippetId").value(snippetId.toString()))
            .andExpect(jsonPath("$[0].level").value("READ"))
    }
}
