package ingsis.snippet.permission.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

/**
 * Proyección local de la identidad de Auth0. `userId` es el claim `sub`; el resto es
 * información de presentación que el propio usuario registra al iniciar sesión.
 */
@Entity
@Table(name = "users")
class UserProfile(
    @Id
    @Column(name = "user_id")
    val userId: String,
    @Column(nullable = false)
    var name: String,
    @Column
    var email: String? = null,
    @Column(name = "first_seen_at", nullable = false)
    val firstSeenAt: Instant = Instant.now(),
    @Column(name = "last_seen_at", nullable = false)
    var lastSeenAt: Instant = Instant.now(),
)
