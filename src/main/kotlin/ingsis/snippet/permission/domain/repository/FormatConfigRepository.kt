package ingsis.snippet.permission.domain.repository

import ingsis.snippet.permission.domain.model.FormatConfig
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FormatConfigRepository : JpaRepository<FormatConfig, String>
