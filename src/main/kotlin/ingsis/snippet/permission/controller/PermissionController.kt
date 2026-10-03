package ingsis.snippet.permission.controller

import ingsis.snippet.permission.controller.dto.PermissionResponse
import ingsis.snippet.permission.controller.dto.ShareSnippetRequest
import ingsis.snippet.permission.domain.model.PermissionLevel
import ingsis.snippet.permission.service.PermissionService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * API pública de permisos. Toda la identidad sale del claim `sub` del JWT: ningún
 * endpoint acepta un `userId` arbitrario del cliente. Las consultas sobre terceros y la
 * asignación de ownership viven en [InternalPermissionController].
 */
@RestController
@RequestMapping("/api/permissions")
class PermissionController(
    private val permissionService: PermissionService,
) {
    @GetMapping("/{snippetId}")
    fun getMyPermission(
        @PathVariable snippetId: UUID,
        @AuthenticationPrincipal jwt: Jwt,
    ): Map<String, PermissionLevel> {
        val userId = jwt.subject
        val level = permissionService.getPermission(snippetId, userId)
        return mapOf("level" to level)
    }

    @GetMapping("/me")
    fun listMyPermissions(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam(required = false) level: PermissionLevel?,
    ): List<PermissionResponse> {
        val userId = jwt.subject
        return permissionService.getUserPermissions(userId, level)
    }

    @PostMapping("/{snippetId}/share")
    fun shareSnippet(
        @PathVariable snippetId: UUID,
        @AuthenticationPrincipal jwt: Jwt,
        @Valid @RequestBody request: ShareSnippetRequest,
    ): PermissionResponse {
        val requesterId = jwt.subject
        return permissionService.shareSnippet(snippetId, requesterId, request)
    }

    @GetMapping("/{snippetId}/shares")
    fun listShares(
        @PathVariable snippetId: UUID,
        @AuthenticationPrincipal jwt: Jwt,
    ): List<PermissionResponse> {
        val requesterId = jwt.subject
        return permissionService.listShares(snippetId, requesterId)
    }

    @DeleteMapping("/{snippetId}/share/{targetUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun revokeShare(
        @PathVariable snippetId: UUID,
        @PathVariable targetUserId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ) {
        val requesterId = jwt.subject
        permissionService.revokeShare(snippetId, requesterId, targetUserId)
    }

    @PostMapping("/{snippetId}/transfer")
    fun transferOwnership(
        @PathVariable snippetId: UUID,
        @RequestParam newOwnerId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): PermissionResponse {
        val currentOwnerId = jwt.subject
        return permissionService.transferOwnership(snippetId, currentOwnerId, newOwnerId)
    }
}
