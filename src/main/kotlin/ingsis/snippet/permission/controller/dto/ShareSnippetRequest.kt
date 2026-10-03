package ingsis.snippet.permission.controller.dto

import ingsis.snippet.permission.domain.model.PermissionLevel
import jakarta.validation.constraints.NotBlank

data class ShareSnippetRequest(
    @field:NotBlank
    val targetUserId: String,
    val level: PermissionLevel = PermissionLevel.READ,
)
