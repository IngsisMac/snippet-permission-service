package ingsis.snippet.permission.service

import ingsis.snippet.permission.controller.dto.ShareSnippetRequest
import ingsis.snippet.permission.domain.model.Ownership
import ingsis.snippet.permission.domain.model.PermissionLevel
import ingsis.snippet.permission.domain.repository.OwnershipRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.web.server.ResponseStatusException
import java.util.Optional
import java.util.UUID

class PermissionServiceTest {
    private lateinit var ownershipRepository: OwnershipRepository
    private lateinit var permissionService: PermissionService

    @BeforeEach
    fun setUp() {
        ownershipRepository = mock()
        permissionService = PermissionService(ownershipRepository)
    }

    @Test
    fun shouldAssignOwnerSuccessfully() {
        val snippetId = UUID.randomUUID()
        val userId = "auth0|owner123"
        val ownership =
            Ownership(
                snippetId = snippetId,
                userId = userId,
                level = PermissionLevel.OWNER,
            )

        whenever(ownershipRepository.findBySnippetIdAndUserId(snippetId, userId)).thenReturn(Optional.empty())
        whenever(ownershipRepository.save(any<Ownership>())).thenReturn(ownership)

        val response = permissionService.assignOwner(snippetId, userId)

        assertNotNull(response)
        assertEquals(snippetId, response.snippetId)
        assertEquals(userId, response.userId)
        assertEquals(PermissionLevel.OWNER, response.level)
        verify(ownershipRepository).save(any<Ownership>())
    }

    @Test
    fun shouldShareSnippetWhenRequesterIsOwner() {
        val snippetId = UUID.randomUUID()
        val ownerId = "auth0|owner123"
        val targetUser = "auth0|collaborator"
        val request = ShareSnippetRequest(targetUserId = targetUser, level = PermissionLevel.WRITE)

        val ownerOwnership = Ownership(snippetId = snippetId, userId = ownerId, level = PermissionLevel.OWNER)
        val targetOwnership = Ownership(snippetId = snippetId, userId = targetUser, level = PermissionLevel.WRITE)

        whenever(
            ownershipRepository.findBySnippetIdAndUserId(snippetId, ownerId)
        ).thenReturn(Optional.of(ownerOwnership))
        whenever(ownershipRepository.findBySnippetIdAndUserId(snippetId, targetUser)).thenReturn(Optional.empty())
        whenever(ownershipRepository.save(any<Ownership>())).thenReturn(targetOwnership)

        val response = permissionService.shareSnippet(snippetId, ownerId, request)

        assertEquals(PermissionLevel.WRITE, response.level)
        assertEquals(targetUser, response.userId)
    }

    @Test
    fun shouldThrowForbiddenWhenSharingAsNonOwner() {
        val snippetId = UUID.randomUUID()
        val nonOwnerId = "auth0|reader"
        val request = ShareSnippetRequest(targetUserId = "auth0|someone", level = PermissionLevel.READ)
        val readerOwnership = Ownership(snippetId = snippetId, userId = nonOwnerId, level = PermissionLevel.READ)

        whenever(
            ownershipRepository.findBySnippetIdAndUserId(snippetId, nonOwnerId)
        ).thenReturn(Optional.of(readerOwnership))

        assertThrows<ResponseStatusException> {
            permissionService.shareSnippet(snippetId, nonOwnerId, request)
        }
    }

    @Test
    fun shouldReturnNoneWhenUserHasNoPermission() {
        val snippetId = UUID.randomUUID()
        val userId = "auth0|stranger"

        whenever(ownershipRepository.findBySnippetIdAndUserId(snippetId, userId)).thenReturn(Optional.empty())

        val level = permissionService.getPermission(snippetId, userId)

        assertEquals(PermissionLevel.NONE, level)
    }

    @Test
    fun shouldRevokeShareWhenRequesterIsOwner() {
        val snippetId = UUID.randomUUID()
        val ownerId = "auth0|owner123"
        val targetUser = "auth0|collaborator"
        val ownerOwnership = Ownership(snippetId = snippetId, userId = ownerId, level = PermissionLevel.OWNER)

        whenever(
            ownershipRepository.findBySnippetIdAndUserId(snippetId, ownerId)
        ).thenReturn(Optional.of(ownerOwnership))

        permissionService.revokeShare(snippetId, ownerId, targetUser)

        verify(ownershipRepository).deleteBySnippetIdAndUserId(snippetId, targetUser)
    }
}
