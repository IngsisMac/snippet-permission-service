package ingsis.snippet.permission.directory

import ingsis.snippet.permission.controller.dto.RegisterProfileRequest
import ingsis.snippet.permission.domain.model.UserProfile
import ingsis.snippet.permission.domain.repository.UserProfileRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.Instant
import java.util.Optional

class UserDirectoryServiceTest {
    private lateinit var repository: UserProfileRepository
    private lateinit var service: UserDirectoryService

    private val requesterId = "auth0|requester"

    @BeforeEach
    fun setUp() {
        repository = mock()
        whenever(repository.save(any<UserProfile>())).thenAnswer { it.arguments[0] }
        service = UserDirectoryService(repository)
    }

    @Test
    fun shouldCreateProfileOnFirstLogin() {
        whenever(repository.findById(requesterId)).thenReturn(Optional.empty())

        val summary = service.registerSelf(requesterId, RegisterProfileRequest(name = "Ana", email = "ana@mail.com"))

        assertEquals(requesterId, summary.id)
        assertEquals("Ana", summary.name)
        assertEquals("ana@mail.com", summary.email)
    }

    @Test
    fun shouldRefreshNameAndLastSeenOnLaterLogins() {
        val firstSeen = Instant.parse("2026-09-01T00:00:00Z")
        val existing =
            UserProfile(
                userId = requesterId,
                name = "Old",
                email = null,
                firstSeenAt = firstSeen,
                lastSeenAt = firstSeen
            )
        whenever(repository.findById(requesterId)).thenReturn(Optional.of(existing))

        val summary = service.registerSelf(requesterId, RegisterProfileRequest(name = "Renamed"))

        assertEquals("Renamed", summary.name)
        assertNull(summary.email)
        assertEquals(firstSeen, existing.firstSeenAt)
        assertTrue(existing.lastSeenAt.isAfter(firstSeen))
    }

    @Test
    fun shouldSearchOthersByNormalizedTermExcludingRequester() {
        val pageable = PageRequest.of(0, 5)
        val other = UserProfile(userId = "auth0|other", name = "Bruno", email = "bruno@mail.com")
        whenever(repository.search(eq(requesterId), eq("bru"), eq(pageable))).thenReturn(PageImpl(listOf(other)))

        val page = service.search(requesterId, "  bru  ", pageable)

        assertEquals(1, page.totalElements)
        assertEquals("auth0|other", page.content.single().id)
        assertEquals("Bruno", page.content.single().name)
    }

    @Test
    fun shouldTreatBlankTermAsNoFilter() {
        val pageable = PageRequest.of(0, 5)
        whenever(repository.search(eq(requesterId), isNull(), eq(pageable))).thenReturn(PageImpl(emptyList()))

        service.search(requesterId, "   ", pageable)

        val captor = argumentCaptor<String>()
        verify(repository).search(eq(requesterId), captor.capture(), eq(pageable))
        assertNull(captor.firstValue)
    }
}
