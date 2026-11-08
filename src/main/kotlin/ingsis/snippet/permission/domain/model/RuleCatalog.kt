package ingsis.snippet.permission.domain.model

import org.springframework.boot.context.properties.ConfigurationProperties

enum class RuleKind {
    BOOLEAN,
    NUMBER,
    STRING,
}

/**
 * Una regla que el formatter o el linter entienden. El nombre es la clave exacta que viaja en el
 * mapa de configuración hacia el runner (las del TCK); `allowedValues` acota los valores válidos
 * cuando la regla es un enumerado, para que la interfaz pueda ofrecer una lista en lugar de un
 * campo de texto libre.
 */
data class RuleDefinition(
    val name: String,
    val kind: RuleKind,
    val defaultValue: Any? = null,
    val allowedValues: List<String> = emptyList(),
)

/**
 * Catálogo de reglas configurables, fuente de verdad del backend.
 *
 * Vive acá y no en el runner porque este es el servicio dueño del dominio "reglas del usuario" y
 * el que tiene que rechazar un `PUT` con claves desconocidas: ponerlo en el runner le agregaría
 * una dependencia de arranque entre servicios. Antes el catálogo estaba duplicado en la interfaz,
 * que podía mandar cualquier mapa y el backend lo guardaba tal cual.
 */
@ConfigurationProperties(prefix = "permission.rule-catalog")
data class RuleCatalogProperties(
    val lint: List<RuleDefinition> = DEFAULT_LINT_RULES,
    val format: List<RuleDefinition> = DEFAULT_FORMAT_RULES,
) {
    companion object {
        const val CAMEL_CASE = "camel case"
        const val SNAKE_CASE = "snake case"

        val DEFAULT_LINT_RULES =
            listOf(
                RuleDefinition("identifier_format", RuleKind.STRING, CAMEL_CASE, listOf(CAMEL_CASE, SNAKE_CASE)),
                RuleDefinition("mandatory-variable-or-literal-in-println", RuleKind.BOOLEAN),
                RuleDefinition("mandatory-variable-or-literal-in-readInput", RuleKind.BOOLEAN),
                RuleDefinition("no-unused-variables", RuleKind.BOOLEAN),
                RuleDefinition("no-empty-println", RuleKind.BOOLEAN),
            )

        val DEFAULT_FORMAT_RULES =
            listOf(
                RuleDefinition("enforce-spacing-around-equals", RuleKind.BOOLEAN),
                RuleDefinition("enforce-no-spacing-around-equals", RuleKind.BOOLEAN),
                RuleDefinition("enforce-spacing-before-colon-in-declaration", RuleKind.BOOLEAN),
                RuleDefinition("enforce-spacing-after-colon-in-declaration", RuleKind.BOOLEAN),
                RuleDefinition("mandatory-single-space-separation", RuleKind.BOOLEAN),
                RuleDefinition("mandatory-space-surrounding-operations", RuleKind.BOOLEAN),
                RuleDefinition("mandatory-line-break-after-statement", RuleKind.BOOLEAN),
                RuleDefinition("line-breaks-after-println", RuleKind.NUMBER, 1),
                RuleDefinition("if-brace-same-line", RuleKind.BOOLEAN),
                RuleDefinition("if-brace-below-line", RuleKind.BOOLEAN),
                RuleDefinition("indent-inside-if", RuleKind.NUMBER, 4),
                RuleDefinition("enforce-no-space-before-semicolon", RuleKind.BOOLEAN),
                RuleDefinition("max-blank-lines-between-statements", RuleKind.NUMBER, 1),
            )
    }
}

/** Qué catálogo aplica a un pedido de reglas. */
enum class RuleType {
    LINT,
    FORMAT,
}
