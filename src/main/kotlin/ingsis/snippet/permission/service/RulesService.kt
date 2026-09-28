package ingsis.snippet.permission.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import ingsis.snippet.permission.controller.dto.RulesResponse
import ingsis.snippet.permission.controller.dto.UpdateRulesRequest
import ingsis.snippet.permission.domain.model.FormatConfig
import ingsis.snippet.permission.domain.model.LintConfig
import ingsis.snippet.permission.domain.repository.FormatConfigRepository
import ingsis.snippet.permission.domain.repository.LintConfigRepository
import ingsis.snippet.permission.redis.RuleChangePublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class RulesService(
    private val lintConfigRepository: LintConfigRepository,
    private val formatConfigRepository: FormatConfigRepository,
    private val objectMapper: ObjectMapper,
    private val ruleChangePublisher: RuleChangePublisher? = null,
) {
    @Transactional(readOnly = true)
    fun getLintRules(userId: String): RulesResponse {
        val config =
            lintConfigRepository.findById(userId).orElseGet {
                LintConfig(userId = userId)
            }
        return RulesResponse(
            userId = config.userId,
            rulesVersion = config.rulesVersion,
            rules = parseRules(config.rules),
            updatedAt = config.updatedAt,
        )
    }

    @Transactional
    fun updateLintRules(
        userId: String,
        request: UpdateRulesRequest,
    ): RulesResponse {
        val config =
            lintConfigRepository.findById(userId).orElseGet {
                LintConfig(userId = userId, rulesVersion = 0)
            }
        config.rulesVersion += 1
        config.rules = objectMapper.writeValueAsString(request.rules)
        config.updatedAt = Instant.now()
        val saved = lintConfigRepository.save(config)
        val parsedRules = parseRules(saved.rules)

        ruleChangePublisher?.publishLintRuleChange(userId, saved.rulesVersion, parsedRules)

        return RulesResponse(
            userId = saved.userId,
            rulesVersion = saved.rulesVersion,
            rules = parsedRules,
            updatedAt = saved.updatedAt,
        )
    }

    @Transactional(readOnly = true)
    fun getFormatRules(userId: String): RulesResponse {
        val config =
            formatConfigRepository.findById(userId).orElseGet {
                FormatConfig(userId = userId)
            }
        return RulesResponse(
            userId = config.userId,
            rulesVersion = config.rulesVersion,
            rules = parseRules(config.rules),
            updatedAt = config.updatedAt,
        )
    }

    @Transactional
    fun updateFormatRules(
        userId: String,
        request: UpdateRulesRequest,
    ): RulesResponse {
        val config =
            formatConfigRepository.findById(userId).orElseGet {
                FormatConfig(userId = userId, rulesVersion = 0)
            }
        config.rulesVersion += 1
        config.rules = objectMapper.writeValueAsString(request.rules)
        config.updatedAt = Instant.now()
        val saved = formatConfigRepository.save(config)
        val parsedRules = parseRules(saved.rules)

        ruleChangePublisher?.publishFormatRuleChange(userId, saved.rulesVersion, parsedRules)

        return RulesResponse(
            userId = saved.userId,
            rulesVersion = saved.rulesVersion,
            rules = parsedRules,
            updatedAt = saved.updatedAt,
        )
    }

    private fun parseRules(json: String): Map<String, Any> =
        try {
            objectMapper.readValue(json, object : TypeReference<Map<String, Any>>() {})
        } catch (_: Exception) {
            emptyMap()
        }
}
