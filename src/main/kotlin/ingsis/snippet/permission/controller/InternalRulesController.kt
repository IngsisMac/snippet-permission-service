package ingsis.snippet.permission.controller

import ingsis.snippet.permission.controller.dto.RulesResponse
import ingsis.snippet.permission.service.RulesService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Lectura de reglas de un usuario arbitrario, para que `snippet-service` pueda formatear o
 * lintear con las reglas del dueño del snippet. Solo accesible desde la red interna.
 */
@RestController
@RequestMapping("/internal/rules")
class InternalRulesController(
    private val rulesService: RulesService,
) {
    @GetMapping("/lint/{userId}")
    fun getUserLintRules(
        @PathVariable userId: String,
    ): RulesResponse = rulesService.getLintRules(userId)

    @GetMapping("/format/{userId}")
    fun getUserFormatRules(
        @PathVariable userId: String,
    ): RulesResponse = rulesService.getFormatRules(userId)
}
