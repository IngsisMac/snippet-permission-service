package ingsis.snippet.permission.controller

import ingsis.snippet.permission.controller.dto.PermissionResponse
import ingsis.snippet.permission.controller.dto.ShareSnippetRequest
import ingsis.snippet.permission.domain.model.PermissionLevel
import ingsis.snippet.permission.service.PermissionService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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

@RestController
@RequestMapping("/api/permissions")
class PermissionController(
    private val permissionService: PermissionService,
) {
    @PostMapping
    fun assignOwner(
        @RequestParam snippetId: UUID,
        @RequestParam userId: String,
    ): ResponseEntity<PermissionResponse> {
        val created = permissionService.assignOwner(snippetId, userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @GetMapping("/{snippetId}")
    fun getPermission(
        @PathVariable snippetId: UUID,
        @RequestParam(required = false) user: String?,
        @AuthenticationPrincipal jwt: Jwt?,
    ): Map<String, PermissionLevel> {
        val targetUser = user ?: jwt?.subject ?: "anonymous"
        val level = permissionService.getPermission(snippetId, targetUser)
        return mapOf("level" to level)
    }

    @PostMapping("/{snippetId}/share")
    fun shareSnippet(
        @PathVariable snippetId: UUID,
        @AuthenticationPrincipal jwt: Jwt?,
        @Valid @RequestBody request: ShareSnippetRequest,
    ): PermissionResponse {
        val requesterId = jwt?.subject ?: "anonymous"
        return permissionService.shareSnippet(snippetId, requesterId, request)
    }

    @GetMapping("/{snippetId}/shares")
    fun listShares(
        @PathVariable snippetId: UUID,
        @AuthenticationPrincipal jwt: Jwt?,
    ): List<PermissionResponse> {
        val requesterId = jwt?.subject ?: "anonymous"
        return permissionService.listShares(snippetId, requesterId)
    }

    @DeleteMapping("/{snippetId}/share/{targetUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun revokeShare(
        @PathVariable snippetId: UUID,
        @PathVariable targetUserId: String,
        @AuthenticationPrincipal jwt: Jwt?,
    ) {
        val requesterId = jwt?.subject ?: "anonymous"
        permissionService.revokeShare(snippetId, requesterId, targetUserId)
    }

    @GetMapping("/user/{userId}")
    fun listUserPermissions(
        @PathVariable userId: String,
        @RequestParam(required = false) level: PermissionLevel?,
    ): List<PermissionResponse> = permissionService.getUserPermissions(userId, level)

    @PostMapping("/{snippetId}/transfer")
    fun transferOwnership(
        @PathVariable snippetId: UUID,
        @RequestParam newOwnerId: String,
        @AuthenticationPrincipal jwt: Jwt?,
    ): PermissionResponse {
        val currentOwnerId = jwt?.subject ?: "anonymous"
        return permissionService.transferOwnership(snippetId, currentOwnerId, newOwnerId)
    }
}
