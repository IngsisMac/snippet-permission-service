package ingsis.snippet.permission.domain.repository

import ingsis.snippet.permission.domain.model.Ownership
import ingsis.snippet.permission.domain.model.PermissionLevel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface OwnershipRepository : JpaRepository<Ownership, UUID> {
    fun findBySnippetIdAndUserId(
        snippetId: UUID,
        userId: String,
    ): Optional<Ownership>

    fun findAllBySnippetId(snippetId: UUID): List<Ownership>

    fun findAllByUserId(userId: String): List<Ownership>

    fun findAllByUserIdAndLevel(
        userId: String,
        level: PermissionLevel,
    ): List<Ownership>

    fun deleteBySnippetIdAndUserId(
        snippetId: UUID,
        userId: String,
    )
}
