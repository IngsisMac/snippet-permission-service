package ingsis.snippet.permission.directory

import ingsis.snippet.permission.controller.dto.RegisterProfileRequest
import ingsis.snippet.permission.controller.dto.UserSummaryResponse
import ingsis.snippet.permission.domain.model.UserProfile
import ingsis.snippet.permission.domain.repository.UserProfileRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Directorio de usuarios de la plataforma (D11). Es una proyección local de las identidades
 * de Auth0: se alimenta cuando cada usuario inicia sesión (`registerSelf`) y, cuando exista
 * la Application M2M sobre la Management API, también por una sincronización periódica. La
 * búsqueda para compartir snippets siempre resuelve acá, sin llamar a Auth0 en el camino
 * caliente del autocomplete.
 */
@Service
class UserDirectoryService(
    private val repository: UserProfileRepository,
) {
    @Transactional
    fun registerSelf(
        userId: String,
        request: RegisterProfileRequest,
    ): UserSummaryResponse {
        val now = Instant.now()
        val profile =
            repository.findById(userId).orElseGet {
                UserProfile(userId = userId, name = request.name, email = request.email, firstSeenAt = now)
            }
        profile.name = request.name
        profile.email = request.email
        profile.lastSeenAt = now
        return toSummary(repository.save(profile))
    }

    @Transactional(readOnly = true)
    fun search(
        requesterId: String,
        term: String?,
        pageable: Pageable,
    ): Page<UserSummaryResponse> {
        val normalized = term?.trim()?.takeIf { it.isNotEmpty() }
        return repository.search(requesterId, normalized, pageable).map { toSummary(it) }
    }

    private fun toSummary(profile: UserProfile) =
        UserSummaryResponse(id = profile.userId, name = profile.name, email = profile.email)
}
