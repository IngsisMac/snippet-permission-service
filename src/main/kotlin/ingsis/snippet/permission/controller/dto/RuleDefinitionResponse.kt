package ingsis.snippet.permission.controller.dto

import ingsis.snippet.permission.domain.model.RuleDefinition

/**
 * Catálogo tal como lo consume la interfaz: necesita la lista completa para mostrar también las
 * reglas apagadas, saber qué control dibujar por regla y, cuando hay `allowedValues`, ofrecer una
 * lista en lugar de un campo de texto libre.
 */
data class RuleDefinitionResponse(
    val name: String,
    val kind: String,
    val defaultValue: Any?,
    val allowedValues: List<String>,
) {
    companion object {
        fun from(definition: RuleDefinition): RuleDefinitionResponse =
            RuleDefinitionResponse(
                name = definition.name,
                kind = definition.kind.name.lowercase(),
                defaultValue = definition.defaultValue,
                allowedValues = definition.allowedValues,
            )
    }
}
