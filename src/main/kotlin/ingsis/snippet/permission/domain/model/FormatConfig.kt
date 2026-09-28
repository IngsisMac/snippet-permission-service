package ingsis.snippet.permission.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "format_configs")
class FormatConfig(
    @Id
    @Column(name = "user_id")
    val userId: String,
    @Column(name = "rules_version", nullable = false)
    var rulesVersion: Int = 1,
    @Column(nullable = false, columnDefinition = "TEXT")
    var rules: String = "{}",
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
)
