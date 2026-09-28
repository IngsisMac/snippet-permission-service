package ingsis.snippet.permission.domain.repository

import ingsis.snippet.permission.domain.model.LintConfig
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LintConfigRepository : JpaRepository<LintConfig, String>
