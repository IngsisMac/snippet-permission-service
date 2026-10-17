package ingsis.snippet.permission.redis

import org.austral.ingsis.redis.RedisStreamProducer
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class LintRuleChangeProducer(
    redisTemplate: RedisTemplate<String, String>,
) : RedisStreamProducer(streamKey = "lint-rules", redis = redisTemplate)
