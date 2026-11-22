package ingsis.snippet.permission.controller

import ingsis.snippet.permission.config.SecurityConfig
import ingsis.snippet.permission.directory.UserDirectoryService
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

@WebMvcTest(InternalUserController::class)
@Import(SecurityConfig::class)
class InternalUserControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var directory: UserDirectoryService

    @Test
    fun shouldReturnTheNameOfEachKnownUser() {
        whenever(directory.namesOf(listOf("auth0|ana", "auth0|bot")))
            .thenReturn(mapOf("auth0|ana" to "Ana"))

        mockMvc
            .perform(get("/internal/users/names").param("ids", "auth0|ana", "auth0|bot").with(jwt()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$['auth0|ana']").value("Ana"))
            .andExpect(jsonPath("$['auth0|bot']").doesNotExist())
    }
}
