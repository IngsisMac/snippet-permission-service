package ingsis.snippet.permission.controller.dto

import ingsis.snippet.permission.domain.model.PermissionLevel
import java.time.Instant
import java.util.UUID

data class PermissionResponse(
    val snippetId: UUID,
    val userId: String,
    val level: PermissionLevel,
    val grantedAt: Instant,
)
