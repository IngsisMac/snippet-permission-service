package ingsis.snippet.permission.service

import ingsis.snippet.permission.controller.dto.PermissionResponse
import ingsis.snippet.permission.controller.dto.ShareSnippetRequest
import ingsis.snippet.permission.domain.model.Ownership
import ingsis.snippet.permission.domain.model.PermissionLevel
import ingsis.snippet.permission.domain.repository.OwnershipRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class PermissionService(
    private val ownershipRepository: OwnershipRepository,
) {
    @Transactional
    fun assignOwner(
        snippetId: UUID,
        userId: String,
    ): PermissionResponse {
        val existing = ownershipRepository.findBySnippetIdAndUserId(snippetId, userId)
        if (existing.isPresent) {
            existing.get().level = PermissionLevel.OWNER
            return mapToResponse(ownershipRepository.save(existing.get()))
        }
        val ownership =
            Ownership(
                snippetId = snippetId,
                userId = userId,
                level = PermissionLevel.OWNER,
            )
        return mapToResponse(ownershipRepository.save(ownership))
    }

    @Transactional
    fun shareSnippet(
        snippetId: UUID,
        requesterId: String,
        request: ShareSnippetRequest,
    ): PermissionResponse {
        val requesterLevel = getPermission(snippetId, requesterId)
        if (requesterLevel != PermissionLevel.OWNER) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only owners can share snippets")
        }
        assertShareRequestIsValid(requesterId, request)

        val target =
            ownershipRepository
                .findBySnippetIdAndUserId(snippetId, request.targetUserId)
                .orElse(
                    Ownership(
                        snippetId = snippetId,
                        userId = request.targetUserId,
                        level = request.level,
                    ),
                )
        target.level = request.level
        return mapToResponse(ownershipRepository.save(target))
    }

    @Transactional(readOnly = true)
    fun getPermission(
        snippetId: UUID,
        userId: String,
    ): PermissionLevel =
        ownershipRepository
            .findBySnippetIdAndUserId(snippetId, userId)
            .map { it.level }
            .orElse(PermissionLevel.NONE)

    @Transactional(readOnly = true)
    fun listShares(
        snippetId: UUID,
        requesterId: String,
    ): List<PermissionResponse> {
        val level = getPermission(snippetId, requesterId)
        if (level == PermissionLevel.NONE) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "No access to this snippet")
        }
        return ownershipRepository.findAllBySnippetId(snippetId).map { mapToResponse(it) }
    }

    @Transactional
    fun revokeShare(
        snippetId: UUID,
        requesterId: String,
        targetUserId: String,
    ) {
        val level = getPermission(snippetId, requesterId)
        if (level != PermissionLevel.OWNER) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only owners can revoke shares")
        }
        ownershipRepository.deleteBySnippetIdAndUserId(snippetId, targetUserId)
    }

    @Transactional(readOnly = true)
    fun getUserPermissions(
        userId: String,
        level: PermissionLevel? = null,
    ): List<PermissionResponse> {
        val records =
            if (level != null) {
                ownershipRepository.findAllByUserIdAndLevel(userId, level)
            } else {
                ownershipRepository.findAllByUserId(userId)
            }
        return records.map { mapToResponse(it) }
    }

    @Transactional
    fun transferOwnership(
        snippetId: UUID,
        currentOwnerId: String,
        newOwnerId: String,
    ): PermissionResponse {
        val currentLevel = getPermission(snippetId, currentOwnerId)
        if (currentLevel != PermissionLevel.OWNER) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only owners can transfer ownership")
        }
        val currentOwner =
            ownershipRepository
                .findBySnippetIdAndUserId(snippetId, currentOwnerId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Current owner not found") }

        val newOwner =
            ownershipRepository
                .findBySnippetIdAndUserId(snippetId, newOwnerId)
                .orElse(
                    Ownership(
                        snippetId = snippetId,
                        userId = newOwnerId,
                        level = PermissionLevel.OWNER,
                    ),
                )
        newOwner.level = PermissionLevel.OWNER
        currentOwner.level = PermissionLevel.WRITE
        ownershipRepository.save(currentOwner)
        return mapToResponse(ownershipRepository.save(newOwner))
    }

    private fun assertShareRequestIsValid(
        requesterId: String,
        request: ShareSnippetRequest,
    ) {
        if (request.level !in SHAREABLE_LEVELS) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Snippets can only be shared with READ or WRITE level; use transfer to change the owner",
            )
        }
        if (request.targetUserId == requesterId) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Owners cannot share a snippet with themselves")
        }
    }

    private fun mapToResponse(ownership: Ownership): PermissionResponse =
        PermissionResponse(
            snippetId = ownership.snippetId,
            userId = ownership.userId,
            level = ownership.level,
            grantedAt = ownership.grantedAt,
        )

    companion object {
        private val SHAREABLE_LEVELS = setOf(PermissionLevel.READ, PermissionLevel.WRITE)
    }
}
