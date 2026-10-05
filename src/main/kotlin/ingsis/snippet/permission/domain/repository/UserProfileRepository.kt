package ingsis.snippet.permission.domain.repository

import ingsis.snippet.permission.domain.model.UserProfile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant

/** `CAST(:term AS string)`: con `null` PostgreSQL no infiere el tipo del bind y falla en `LOWER`. */
@Repository
interface UserProfileRepository : JpaRepository<UserProfile, String> {
    /**
     * Alta o refresco atómico del perfil. Dos logins simultáneos del mismo usuario (la UI puede
     * disparar el registro más de una vez al montar) no deben competir con un `find` + `save`:
     * el `ON CONFLICT` deja la decisión en la base y conserva `first_seen_at` del primer alta.
     */
    @Modifying(clearAutomatically = true)
    @Query(
        value = """
        INSERT INTO users (user_id, name, email, first_seen_at, last_seen_at)
        VALUES (:userId, :name, :email, :seenAt, :seenAt)
        ON CONFLICT (user_id) DO UPDATE
            SET name = EXCLUDED.name,
                email = EXCLUDED.email,
                last_seen_at = EXCLUDED.last_seen_at
        """,
        nativeQuery = true,
    )
    fun upsert(
        @Param("userId") userId: String,
        @Param("name") name: String,
        @Param("email") email: String?,
        @Param("seenAt") seenAt: Instant,
    )

    @Query(
        """
        SELECT u FROM UserProfile u
        WHERE u.userId <> :excludedUserId
          AND (CAST(:term AS string) IS NULL
               OR LOWER(u.name) LIKE LOWER(CONCAT('%', CAST(:term AS string), '%'))
               OR LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', CAST(:term AS string), '%')))
        ORDER BY LOWER(u.name) ASC
        """,
    )
    fun search(
        @Param("excludedUserId") excludedUserId: String,
        @Param("term") term: String?,
        pageable: Pageable,
    ): Page<UserProfile>
}
