package ingsis.snippet.permission.service

import ingsis.snippet.permission.domain.model.RuleCatalogProperties
import ingsis.snippet.permission.domain.model.RuleDefinition
import ingsis.snippet.permission.domain.model.RuleKind
import ingsis.snippet.permission.domain.model.RuleType
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Expone el catálogo de reglas y valida contra él lo que llega en un `PUT`.
 *
 * Antes se guardaba cualquier mapa que mandara el cliente: una clave con un error de tipeo se
 * persistía, se publicaba a Redis y el runner la ignoraba en silencio, así que el usuario veía su
 * regla "activa" sin ningún efecto. Ahora una clave desconocida o un valor del tipo equivocado
 * cortan con `400` antes de tocar la base.
 */
@Service
class RuleCatalogService(
    private val catalog: RuleCatalogProperties,
) {
    fun definitions(type: RuleType): List<RuleDefinition> =
        when (type) {
            RuleType.LINT -> catalog.lint
            RuleType.FORMAT -> catalog.format
        }

    fun validate(
        type: RuleType,
        rules: Map<String, Any>,
    ): Map<String, Any> {
        val byName = definitions(type).associateBy { it.name }
        rules.forEach { (name, value) ->
            val definition =
                byName[name] ?: reject("Unknown ${type.name.lowercase()} rule '$name'")
            validateValue(definition, value)
        }
        return rules
    }

    private fun validateValue(
        definition: RuleDefinition,
        value: Any?,
    ) {
        when (definition.kind) {
            RuleKind.BOOLEAN ->
                if (value !is Boolean) {
                    reject("Rule '${definition.name}' expects a boolean, got ${describe(value)}")
                }
            RuleKind.NUMBER -> validateNumber(definition, value)
            RuleKind.STRING -> validateString(definition, value)
        }
    }

    private fun validateNumber(
        definition: RuleDefinition,
        value: Any?,
    ) {
        if (value !is Number) {
            reject("Rule '${definition.name}' expects a number, got ${describe(value)}")
        }
        if (value.toDouble() < 0) {
            reject("Rule '${definition.name}' expects a non-negative number, got $value")
        }
    }

    private fun validateString(
        definition: RuleDefinition,
        value: Any?,
    ) {
        if (value !is String) {
            reject("Rule '${definition.name}' expects a string, got ${describe(value)}")
        }
        if (definition.allowedValues.isNotEmpty() && value !in definition.allowedValues) {
            reject(
                "Rule '${definition.name}' accepts only ${definition.allowedValues.joinToString(" or ") { "'$it'" }}," +
                    " got '$value'",
            )
        }
    }

    private fun describe(value: Any?): String = if (value == null) "null" else value::class.simpleName ?: "unknown"

    private fun reject(message: String): Nothing = throw ResponseStatusException(HttpStatus.BAD_REQUEST, message)
}
