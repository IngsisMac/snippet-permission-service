package ingsis.snippet.permission.directory

import ingsis.snippet.permission.controller.dto.RegisterProfileRequest
import ingsis.snippet.permission.domain.model.UserProfile
import ingsis.snippet.permission.domain.repository.UserProfileRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
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
import java.util.Optional

class UserDirectoryServiceTest {
    private lateinit var repository: UserProfileRepository
    private lateinit var service: UserDirectoryService

    private val requesterId = "auth0|requester"

    @BeforeEach
    fun setUp() {
        repository = mock()
        service = UserDirectoryService(repository)
    }

    @Test
    fun shouldUpsertProfileAndReturnPersistedSummary() {
        val request = RegisterProfileRequest(name = "Ana", email = "ana@mail.com")
        val persisted = UserProfile(userId = requesterId, name = "Ana", email = "ana@mail.com")
        whenever(repository.findById(requesterId)).thenReturn(Optional.of(persisted))

        val summary = service.registerSelf(requesterId, request)

        verify(repository).upsert(eq(requesterId), eq("Ana"), eq("ana@mail.com"), any())
        assertEquals(requesterId, summary.id)
        assertEquals("Ana", summary.name)
        assertEquals("ana@mail.com", summary.email)
    }

    @Test
    fun shouldUpsertWithoutEmailWhenTokenHasNone() {
        val persisted = UserProfile(userId = requesterId, name = "Renamed", email = null)
        whenever(repository.findById(requesterId)).thenReturn(Optional.of(persisted))

        val summary = service.registerSelf(requesterId, RegisterProfileRequest(name = "Renamed"))

        verify(repository).upsert(eq(requesterId), eq("Renamed"), isNull(), any())
        assertEquals("Renamed", summary.name)
        assertNull(summary.email)
    }

    @Test
    fun shouldFailLoudlyIfProfileIsMissingAfterUpsert() {
        whenever(repository.findById(requesterId)).thenReturn(Optional.empty())

        assertThrows(IllegalStateException::class.java) {
            service.registerSelf(requesterId, RegisterProfileRequest(name = "Ana"))
        }
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
