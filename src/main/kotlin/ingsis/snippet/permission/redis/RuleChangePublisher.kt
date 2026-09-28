package ingsis.snippet.permission.redis

interface RuleChangePublisher {
    fun publishLintRuleChange(
        userId: String,
        rulesVersion: Int,
        rules: Map<String, Any>,
    )

    fun publishFormatRuleChange(
        userId: String,
        rulesVersion: Int,
        rules: Map<String, Any>,
    )
}
