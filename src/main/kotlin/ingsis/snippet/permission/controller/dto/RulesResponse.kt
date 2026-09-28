package ingsis.snippet.permission.controller.dto

import java.time.Instant

data class RulesResponse(
    val userId: String,
    val rulesVersion: Int,
    val rules: Map<String, Any>,
    val updatedAt: Instant,
)
