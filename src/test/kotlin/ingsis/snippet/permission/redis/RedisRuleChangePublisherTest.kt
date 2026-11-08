package ingsis.snippet.permission.redis

import com.fasterxml.jackson.databind.ObjectMapper
import ingsis.snippet.redis.event.FormatRulesChangeEvent
import ingsis.snippet.redis.event.LintRulesChangeEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StreamOperations

class RedisRuleChangePublisherTest {
    private lateinit var redisTemplate: RedisTemplate<String, String>
    private lateinit var streamOperations: StreamOperations<String, Any, Any>
    private lateinit var lintProducer: LintRuleChangeProducer
    private lateinit var formatProducer: FormatRuleChangeProducer
    private lateinit var publisher: RedisRuleChangePublisher
    private val objectMapper = ObjectMapper()

    @BeforeEach
    fun setUp() {
        redisTemplate = mock()
        streamOperations = mock()
        whenever(redisTemplate.opsForStream<Any, Any>()).thenReturn(streamOperations)
        lintProducer = LintRuleChangeProducer(redisTemplate)
        formatProducer = FormatRuleChangeProducer(redisTemplate)
        publisher = RedisRuleChangePublisher(lintProducer, formatProducer, objectMapper)
    }

    @Test
    fun shouldPublishLintRuleChangeEventWithExpectedPayload() {
        whenever(streamOperations.add(any())).thenReturn(RecordId.of("1000-0"))

        publisher.publishLintRuleChange("auth0|u1", 2, mapOf("rule" to "value"))

        val captor = argumentCaptor<ObjectRecord<String, LintRulesChangeEvent>>()
        verify(streamOperations).add(captor.capture())

        val record = captor.firstValue
        assertNotNull(record)
        assertEquals("lint-rules", record.stream)
        assertEquals("auth0|u1", record.value.userId)
        assertEquals(2, record.value.rulesVersion)
        assertEquals("{\"rule\":\"value\"}", record.value.rules)
    }

    @Test
    fun shouldPublishFormatRulesChangeEventWithExpectedPayload() {
        whenever(streamOperations.add(any())).thenReturn(RecordId.of("1001-0"))

        publisher.publishFormatRuleChange("auth0|u2", 3, mapOf("indent" to 4))

        val captor = argumentCaptor<ObjectRecord<String, FormatRulesChangeEvent>>()
        verify(streamOperations).add(captor.capture())

        val record = captor.firstValue
        assertNotNull(record)
        assertEquals("format-rules", record.stream)
        assertEquals("auth0|u2", record.value.userId)
        assertEquals(3, record.value.rulesVersion)
        assertEquals("{\"indent\":4}", record.value.rules)
    }

    @Test
    fun shouldCreateDefaultLintRulesChangeEvent() {
        val event = LintRulesChangeEvent()
        assertEquals("", event.userId)
        assertEquals(0, event.rulesVersion)
        assertEquals("{}", event.rules)
    }

    @Test
    fun shouldCreateDefaultFormatRulesChangeEvent() {
        val event = FormatRulesChangeEvent()
        assertEquals("", event.userId)
        assertEquals(0, event.rulesVersion)
        assertEquals("{}", event.rules)
    }
}
