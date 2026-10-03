package ingsis.snippet.permission.controller

import ingsis.snippet.permission.controller.dto.PermissionResponse
import ingsis.snippet.permission.domain.model.PermissionLevel
import ingsis.snippet.permission.service.PermissionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * API interna consumida solo por otros servicios de la plataforma (hoy `snippet-service`).
 * Acepta `userId` explícito porque el llamador actúa en nombre de un usuario ya autenticado.
 * No se rutea desde el reverse proxy: NGINX responde 403 a todo lo que empieza con `/internal`.
 */
@RestController
@RequestMapping("/internal/permissions")
class InternalPermissionController(
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
    fun getPermissionForUser(
        @PathVariable snippetId: UUID,
        @RequestParam user: String,
    ): Map<String, PermissionLevel> {
        val level = permissionService.getPermission(snippetId, user)
        return mapOf("level" to level)
    }

    @GetMapping("/user/{userId}")
    fun listUserPermissions(
        @PathVariable userId: String,
        @RequestParam(required = false) level: PermissionLevel?,
    ): List<PermissionResponse> = permissionService.getUserPermissions(userId, level)
}
