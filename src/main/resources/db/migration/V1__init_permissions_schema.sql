CREATE TABLE ownership (
    id UUID PRIMARY KEY,
    snippet_id UUID NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    level VARCHAR(50) NOT NULL,
    granted_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_snippet_user UNIQUE (snippet_id, user_id)
);

CREATE TABLE lint_configs (
    user_id VARCHAR(255) PRIMARY KEY,
    rules_version INT NOT NULL DEFAULT 1,
    rules TEXT NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE format_configs (
    user_id VARCHAR(255) PRIMARY KEY,
    rules_version INT NOT NULL DEFAULT 1,
    rules TEXT NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_ownership_snippet ON ownership(snippet_id);
CREATE INDEX idx_ownership_user ON ownership(user_id);
