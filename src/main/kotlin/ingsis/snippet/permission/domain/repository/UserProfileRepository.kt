package ingsis.snippet.permission.domain.repository

import ingsis.snippet.permission.domain.model.UserProfile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/** `CAST(:term AS string)`: con `null` PostgreSQL no infiere el tipo del bind y falla en `LOWER`. */
@Repository
interface UserProfileRepository : JpaRepository<UserProfile, String> {
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
