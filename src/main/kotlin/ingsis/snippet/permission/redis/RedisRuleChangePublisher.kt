package ingsis.snippet.permission.redis

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.StreamRecords
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisRuleChangePublisher(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
) : RuleChangePublisher {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun publishLintRuleChange(
        userId: String,
        rulesVersion: Int,
        rules: Map<String, Any>,
    ) {
        val payload =
            mapOf(
                "userId" to userId,
                "rulesVersion" to rulesVersion.toString(),
                "rules" to objectMapper.writeValueAsString(rules),
                "timestamp" to System.currentTimeMillis().toString(),
            )
        val record: MapRecord<String, String, String> =
            StreamRecords
                .newRecord()
                .ofStrings(payload)
                .withStreamKey(LINT_STREAM)
        redisTemplate.opsForStream<String, String>().add(record)
        logger.info("Published lint rule change to stream {} for user {} v{}", LINT_STREAM, userId, rulesVersion)
    }

    override fun publishFormatRuleChange(
        userId: String,
        rulesVersion: Int,
        rules: Map<String, Any>,
    ) {
        val payload =
            mapOf(
                "userId" to userId,
                "rulesVersion" to rulesVersion.toString(),
                "rules" to objectMapper.writeValueAsString(rules),
                "timestamp" to System.currentTimeMillis().toString(),
            )
        val record: MapRecord<String, String, String> =
            StreamRecords
                .newRecord()
                .ofStrings(payload)
                .withStreamKey(FORMAT_STREAM)
        redisTemplate.opsForStream<String, String>().add(record)
        logger.info("Published format rule change to stream {} for user {} v{}", FORMAT_STREAM, userId, rulesVersion)
    }

    companion object {
        const val LINT_STREAM = "lint-rules-stream"
        const val FORMAT_STREAM = "format-rules-stream"
    }
}
