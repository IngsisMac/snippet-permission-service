package ingsis.snippet.permission.controller.dto

import ingsis.snippet.permission.domain.model.PermissionLevel
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class ShareSnippetRequest(
    @field:NotBlank
    val targetUserId: String,
    @field:NotNull
    val level: PermissionLevel,
)
