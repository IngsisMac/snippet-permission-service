package ingsis.snippet.permission.controller

import ingsis.snippet.permission.controller.dto.RegisterProfileRequest
import ingsis.snippet.permission.controller.dto.UserSummaryResponse
import ingsis.snippet.permission.directory.UserDirectoryService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Directorio de usuarios para compartir snippets (US #7). La identidad siempre sale del JWT:
 * un usuario solo puede registrar su propio perfil, y la búsqueda nunca lo devuelve a él mismo.
 */
@RestController
@RequestMapping("/api/users")
class UserController(
    private val directory: UserDirectoryService,
) {
    @PutMapping("/me")
    fun registerMe(
        @AuthenticationPrincipal jwt: Jwt,
        @Valid @RequestBody request: RegisterProfileRequest,
    ): UserSummaryResponse = directory.registerSelf(jwt.subject, request)

    @GetMapping
    fun searchUsers(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam(required = false) name: String?,
        @PageableDefault(size = 10) pageable: Pageable,
    ): Page<UserSummaryResponse> = directory.search(jwt.subject, name, pageable)
}
