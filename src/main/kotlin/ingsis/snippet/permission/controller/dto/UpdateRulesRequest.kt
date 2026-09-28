package ingsis.snippet.permission.controller.dto

data class UpdateRulesRequest(
    val rules: Map<String, Any> = emptyMap(),
)
