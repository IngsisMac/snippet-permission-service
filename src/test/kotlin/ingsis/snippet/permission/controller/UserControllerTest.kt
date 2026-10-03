package ingsis.snippet.permission.controller

import com.fasterxml.jackson.databind.ObjectMapper
import ingsis.snippet.permission.config.SecurityConfig
import ingsis.snippet.permission.controller.dto.RegisterProfileRequest
import ingsis.snippet.permission.controller.dto.UserSummaryResponse
import ingsis.snippet.permission.directory.UserDirectoryService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(UserController::class)
@Import(SecurityConfig::class)
class UserControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var directory: UserDirectoryService

    private lateinit var other: UserSummaryResponse

    @BeforeEach
    fun setUp() {
        other = UserSummaryResponse(id = "auth0|other", name = "Bruno", email = "bruno@mail.com")
    }

    @Test
    fun shouldRegisterCallerProfileFromTokenSubject() {
        val request = RegisterProfileRequest(name = "Ana", email = "ana@mail.com")
        whenever(directory.registerSelf(eq("auth0|me"), any()))
            .thenReturn(UserSummaryResponse(id = "auth0|me", name = "Ana", email = "ana@mail.com"))

        mockMvc
            .perform(
                put("/api/users/me")
                    .with(jwt().jwt { it.subject("auth0|me") })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("auth0|me"))
            .andExpect(jsonPath("$.name").value("Ana"))
    }

    @Test
    fun shouldRejectBlankNameWhenRegistering() {
        mockMvc
            .perform(
                put("/api/users/me")
                    .with(jwt().jwt { it.subject("auth0|me") })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"  "}"""),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun shouldSearchUsersByNameWithPagination() {
        whenever(directory.search(eq("auth0|me"), eq("bru"), any())).thenReturn(PageImpl(listOf(other)))

        mockMvc
            .perform(
                get("/api/users")
                    .param("name", "bru")
                    .param("page", "0")
                    .param("size", "5")
                    .with(jwt().jwt { it.subject("auth0|me") }),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value("auth0|other"))
            .andExpect(jsonPath("$.content[0].name").value("Bruno"))
            .andExpect(jsonPath("$.totalElements").value(1))
    }

    @Test
    fun shouldRejectUserSearchWithoutToken() {
        mockMvc
            .perform(get("/api/users"))
            .andExpect(status().isUnauthorized)
    }
}
