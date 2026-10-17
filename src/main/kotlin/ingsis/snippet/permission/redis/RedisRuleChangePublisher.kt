package ingsis.snippet.permission.redis

import com.fasterxml.jackson.databind.ObjectMapper
import ingsis.snippet.redis.event.FormatRulesChangeEvent
import ingsis.snippet.redis.event.LintRulesChangeEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class RedisRuleChangePublisher(
    private val lintRuleChangeProducer: LintRuleChangeProducer,
    private val formatRuleChangeProducer: FormatRuleChangeProducer,
    private val objectMapper: ObjectMapper,
) : RuleChangePublisher {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun publishLintRuleChange(
        userId: String,
        rulesVersion: Int,
        rules: Map<String, Any>,
    ) {
        val rulesJson = objectMapper.writeValueAsString(rules)
        val event =
            LintRulesChangeEvent(
                userId = userId,
                rulesVersion = rulesVersion,
                rules = rulesJson,
            )
        lintRuleChangeProducer.emit(event)
        logger.info(
            "Published lint rule change to stream {} for user {} v{}",
            lintRuleChangeProducer.streamKey,
            userId,
            rulesVersion,
        )
    }

    override fun publishFormatRuleChange(
        userId: String,
        rulesVersion: Int,
        rules: Map<String, Any>,
    ) {
        val rulesJson = objectMapper.writeValueAsString(rules)
        val event =
            FormatRulesChangeEvent(
                userId = userId,
                rulesVersion = rulesVersion,
                rules = rulesJson,
            )
        formatRuleChangeProducer.emit(event)
        logger.info(
            "Published format rule change to stream {} for user {} v{}",
            formatRuleChangeProducer.streamKey,
            userId,
            rulesVersion,
        )
    }
}
