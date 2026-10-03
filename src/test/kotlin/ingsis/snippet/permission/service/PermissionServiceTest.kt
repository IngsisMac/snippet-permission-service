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
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
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

    @Test
    fun shouldReturnAllUserPermissionsWhenNoFilterProvided() {
        val userId = "auth0|user1"
        val snippetId1 = UUID.randomUUID()
        val snippetId2 = UUID.randomUUID()
        val list =
            listOf(
                Ownership(snippetId = snippetId1, userId = userId, level = PermissionLevel.OWNER),
                Ownership(snippetId = snippetId2, userId = userId, level = PermissionLevel.READ),
            )

        whenever(ownershipRepository.findAllByUserId(userId)).thenReturn(list)

        val result = permissionService.getUserPermissions(userId)

        assertEquals(2, result.size)
        assertEquals(snippetId1, result[0].snippetId)
        assertEquals(PermissionLevel.OWNER, result[0].level)
        assertEquals(snippetId2, result[1].snippetId)
        assertEquals(PermissionLevel.READ, result[1].level)
    }

    @Test
    fun shouldReturnFilteredUserPermissionsWhenLevelProvided() {
        val userId = "auth0|user1"
        val snippetId = UUID.randomUUID()
        val list = listOf(Ownership(snippetId = snippetId, userId = userId, level = PermissionLevel.OWNER))

        whenever(ownershipRepository.findAllByUserIdAndLevel(userId, PermissionLevel.OWNER)).thenReturn(list)

        val result = permissionService.getUserPermissions(userId, PermissionLevel.OWNER)

        assertEquals(1, result.size)
        assertEquals(snippetId, result[0].snippetId)
        assertEquals(PermissionLevel.OWNER, result[0].level)
    }

    @Test
    fun shouldTransferOwnershipSuccessfully() {
        val snippetId = UUID.randomUUID()
        val currentOwnerId = "auth0|owner"
        val newOwnerId = "auth0|successor"
        val currentOwnerRecord =
            Ownership(snippetId = snippetId, userId = currentOwnerId, level = PermissionLevel.OWNER)
        val newOwnerRecord = Ownership(snippetId = snippetId, userId = newOwnerId, level = PermissionLevel.OWNER)

        whenever(ownershipRepository.findBySnippetIdAndUserId(snippetId, currentOwnerId))
            .thenReturn(Optional.of(currentOwnerRecord))
        whenever(ownershipRepository.findBySnippetIdAndUserId(snippetId, newOwnerId))
            .thenReturn(Optional.empty())
        whenever(ownershipRepository.save(any<Ownership>())).thenReturn(newOwnerRecord)

        val result = permissionService.transferOwnership(snippetId, currentOwnerId, newOwnerId)

        assertEquals(snippetId, result.snippetId)
        assertEquals(newOwnerId, result.userId)
        assertEquals(PermissionLevel.OWNER, result.level)
        assertEquals(PermissionLevel.WRITE, currentOwnerRecord.level)
    }

    @Test
    fun shouldThrowForbiddenWhenNonOwnerTriesToTransferOwnership() {
        val snippetId = UUID.randomUUID()
        val nonOwnerId = "auth0|editor"
        val newOwnerId = "auth0|successor"
        val nonOwnerRecord = Ownership(snippetId = snippetId, userId = nonOwnerId, level = PermissionLevel.WRITE)

        whenever(ownershipRepository.findBySnippetIdAndUserId(snippetId, nonOwnerId))
            .thenReturn(Optional.of(nonOwnerRecord))

        assertThrows<ResponseStatusException> {
            permissionService.transferOwnership(snippetId, nonOwnerId, newOwnerId)
        }
    }

    @Test
    fun shouldRejectSharingWithOwnerLevel() {
        val snippetId = UUID.randomUUID()
        val ownerId = "auth0|owner123"
        val request = ShareSnippetRequest(targetUserId = "auth0|collaborator", level = PermissionLevel.OWNER)
        val ownerOwnership = Ownership(snippetId = snippetId, userId = ownerId, level = PermissionLevel.OWNER)
        whenever(
            ownershipRepository.findBySnippetIdAndUserId(snippetId, ownerId)
        ).thenReturn(Optional.of(ownerOwnership))

        val exception =
            assertThrows<ResponseStatusException> {
                permissionService.shareSnippet(snippetId, ownerId, request)
            }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
        verify(ownershipRepository, never()).save(any<Ownership>())
    }

    @Test
    fun shouldRejectSharingWithOneself() {
        val snippetId = UUID.randomUUID()
        val ownerId = "auth0|owner123"
        val request = ShareSnippetRequest(targetUserId = ownerId)
        val ownerOwnership = Ownership(snippetId = snippetId, userId = ownerId, level = PermissionLevel.OWNER)
        whenever(
            ownershipRepository.findBySnippetIdAndUserId(snippetId, ownerId)
        ).thenReturn(Optional.of(ownerOwnership))

        val exception =
            assertThrows<ResponseStatusException> {
                permissionService.shareSnippet(snippetId, ownerId, request)
            }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun shouldShareWithReadLevelByDefault() {
        val snippetId = UUID.randomUUID()
        val ownerId = "auth0|owner123"
        val targetUser = "auth0|reader"
        val request = ShareSnippetRequest(targetUserId = targetUser)
        val ownerOwnership = Ownership(snippetId = snippetId, userId = ownerId, level = PermissionLevel.OWNER)
        whenever(
            ownershipRepository.findBySnippetIdAndUserId(snippetId, ownerId)
        ).thenReturn(Optional.of(ownerOwnership))
        whenever(ownershipRepository.findBySnippetIdAndUserId(snippetId, targetUser)).thenReturn(Optional.empty())
        whenever(ownershipRepository.save(any<Ownership>())).thenAnswer { it.arguments[0] }

        val response = permissionService.shareSnippet(snippetId, ownerId, request)

        assertEquals(PermissionLevel.READ, response.level)
        assertEquals(targetUser, response.userId)
    }
}
