package ingsis.snippet.permission.controller

import ingsis.snippet.permission.controller.dto.RulesResponse
import ingsis.snippet.permission.controller.dto.UpdateRulesRequest
import ingsis.snippet.permission.service.RulesService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/permissions/rules")
class RulesController(
    private val rulesService: RulesService,
) {
    @GetMapping("/lint")
    fun getMyLintRules(
        @AuthenticationPrincipal jwt: Jwt?,
    ): RulesResponse {
        val userId = jwt?.subject ?: "anonymous"
        return rulesService.getLintRules(userId)
    }

    @PutMapping("/lint")
    fun updateMyLintRules(
        @AuthenticationPrincipal jwt: Jwt?,
        @RequestBody request: UpdateRulesRequest,
    ): RulesResponse {
        val userId = jwt?.subject ?: "anonymous"
        return rulesService.updateLintRules(userId, request)
    }

    @GetMapping("/format")
    fun getMyFormatRules(
        @AuthenticationPrincipal jwt: Jwt?,
    ): RulesResponse {
        val userId = jwt?.subject ?: "anonymous"
        return rulesService.getFormatRules(userId)
    }

    @PutMapping("/format")
    fun updateMyFormatRules(
        @AuthenticationPrincipal jwt: Jwt?,
        @RequestBody request: UpdateRulesRequest,
    ): RulesResponse {
        val userId = jwt?.subject ?: "anonymous"
        return rulesService.updateFormatRules(userId, request)
    }

    @GetMapping("/lint/{userId}")
    fun getUserLintRules(
        @PathVariable userId: String,
    ): RulesResponse = rulesService.getLintRules(userId)

    @GetMapping("/format/{userId}")
    fun getUserFormatRules(
        @PathVariable userId: String,
    ): RulesResponse = rulesService.getFormatRules(userId)
}
