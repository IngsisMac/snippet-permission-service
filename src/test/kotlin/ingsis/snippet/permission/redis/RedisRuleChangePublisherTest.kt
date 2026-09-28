package ingsis.snippet.permission.redis

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.StreamOperations
import org.springframework.data.redis.core.StringRedisTemplate

class RedisRuleChangePublisherTest {
    private lateinit var redisTemplate: StringRedisTemplate
    private lateinit var streamOperations: StreamOperations<String, String, String>
    private lateinit var publisher: RedisRuleChangePublisher
    private val objectMapper = ObjectMapper()

    @BeforeEach
    fun setUp() {
        redisTemplate = mock()
        streamOperations = mock()
        whenever(redisTemplate.opsForStream<String, String>()).thenReturn(streamOperations)
        publisher = RedisRuleChangePublisher(redisTemplate, objectMapper)
    }

    @Test
    fun shouldPublishLintRuleChangeToStream() {
        whenever(streamOperations.add(any())).thenReturn(RecordId.of("1000-0"))

        publisher.publishLintRuleChange("auth0|u1", 2, mapOf("rule" to "value"))

        verify(streamOperations).add(any())
    }

    @Test
    fun shouldPublishFormatRuleChangeToStream() {
        whenever(streamOperations.add(any())).thenReturn(RecordId.of("1001-0"))

        publisher.publishFormatRuleChange("auth0|u1", 3, mapOf("rule" to true))

        verify(streamOperations).add(any())
    }
}
