package ingsis.snippet.permission.controller.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/** Perfil que el usuario autenticado registra de sí mismo al iniciar sesión. */
data class RegisterProfileRequest(
    @field:NotBlank
    @field:Size(max = 255)
    val name: String,
    @field:Size(max = 255)
    val email: String? = null,
)

data class UserSummaryResponse(
    val id: String,
    val name: String,
    val email: String?,
)
