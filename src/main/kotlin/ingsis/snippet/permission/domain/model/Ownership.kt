package ingsis.snippet.permission.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "ownership")
class Ownership(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "snippet_id", nullable = false)
    val snippetId: UUID,
    @Column(name = "user_id", nullable = false)
    val userId: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var level: PermissionLevel,
    @Column(name = "granted_at", nullable = false)
    val grantedAt: Instant = Instant.now(),
)
