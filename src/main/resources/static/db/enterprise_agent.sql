-- 创建数据库
CREATE DATABASE enterprise_agent
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    CONNECTION LIMIT = -1;

--创建用户表
CREATE TABLE sys_user (
                          id BIGSERIAL PRIMARY KEY,

                          username VARCHAR(64) NOT NULL,
                          password VARCHAR(255) NOT NULL,
                          nickname VARCHAR(64) NOT NULL,

                          role_code VARCHAR(32) NOT NULL DEFAULT 'USER',
                          status SMALLINT NOT NULL DEFAULT 1,

                          avatar_url VARCHAR(500),

                          last_login_at TIMESTAMP,
                          last_login_ip VARCHAR(64),

                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          deleted BOOLEAN NOT NULL DEFAULT FALSE,

                          CONSTRAINT uk_sys_user_username UNIQUE (username),

                          CONSTRAINT ck_sys_user_role_code CHECK (
                              role_code IN (
                                            'USER',
                                            'KNOWLEDGE_ADMIN',
                                            'SYSTEM_ADMIN'
                                  )
                              ),

                          CONSTRAINT ck_sys_user_status CHECK (
                              status IN (0, 1, 2)
                              )
);

CREATE TABLE sys_login_log (
                               id BIGSERIAL PRIMARY KEY,

                               user_id BIGINT,
                               username VARCHAR(64) NOT NULL,

                               operation_type SMALLINT NOT NULL,
                               result SMALLINT NOT NULL,

                               failure_reason VARCHAR(255),
                               ip_address VARCHAR(64),
                               user_agent VARCHAR(500),

                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

--创建知识库
CREATE TABLE knowledge_base (
                                id BIGSERIAL PRIMARY KEY,
                                name VARCHAR(128) NOT NULL,
                                description VARCHAR(1000),
                                cover_url VARCHAR(500),
                                category VARCHAR(32) NOT NULL DEFAULT 'GENERAL',
                                visibility VARCHAR(32) NOT NULL DEFAULT 'PRIVATE',
                                owner_id BIGINT NOT NULL,
                                status SMALLINT NOT NULL DEFAULT 1,
                                document_count INTEGER NOT NULL DEFAULT 0,
                                member_count INTEGER NOT NULL DEFAULT 1,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                deleted BOOLEAN NOT NULL DEFAULT FALSE,

                                CONSTRAINT fk_knowledge_base_owner
                                    FOREIGN KEY (owner_id)
                                        REFERENCES sys_user(id),

                                CONSTRAINT ck_knowledge_base_category
                                    CHECK (
                                        category IN (
                                                     'GENERAL',
                                                     'POLICY',
                                                     'PROJECT',
                                                     'TECHNICAL'
                                            )
                                        ),

                                CONSTRAINT ck_knowledge_base_visibility
                                    CHECK (
                                        visibility IN (
                                                       'PRIVATE',
                                                       'MEMBER',
                                                       'PUBLIC'
                                            )
                                        ),

                                CONSTRAINT ck_knowledge_base_status
                                    CHECK (
                                        status IN (0, 1)
                                        ),

                                CONSTRAINT ck_knowledge_base_document_count
                                    CHECK (
                                        document_count >= 0
                                        ),

                                CONSTRAINT ck_knowledge_base_member_count
                                    CHECK (
                                        member_count >= 0
                                        )
);

--知识库成员表
CREATE TABLE knowledge_base_member (
                                       id BIGSERIAL PRIMARY KEY,
                                       knowledge_base_id BIGINT NOT NULL,
                                       user_id BIGINT NOT NULL,
                                       member_role VARCHAR(32) NOT NULL DEFAULT 'VIEWER',
                                       joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       created_by BIGINT NOT NULL,
                                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       deleted BOOLEAN NOT NULL DEFAULT FALSE,

                                       CONSTRAINT fk_kb_member_knowledge_base
                                           FOREIGN KEY (knowledge_base_id)
                                               REFERENCES knowledge_base(id),

                                       CONSTRAINT fk_kb_member_user
                                           FOREIGN KEY (user_id)
                                               REFERENCES sys_user(id),

                                       CONSTRAINT fk_kb_member_created_by
                                           FOREIGN KEY (created_by)
                                               REFERENCES sys_user(id),

                                       CONSTRAINT uk_kb_member_base_user
                                           UNIQUE (knowledge_base_id, user_id),

                                       CONSTRAINT ck_kb_member_role
                                           CHECK (
                                               member_role IN (
                                                               'VIEWER',
                                                               'EDITOR',
                                                               'ADMIN'
                                                   )
                                               )
);

-- =========================================================
-- 3. knowledge_base 索引
-- =========================================================

CREATE INDEX idx_knowledge_base_owner_id
    ON knowledge_base(owner_id);

CREATE INDEX idx_knowledge_base_status
    ON knowledge_base(status);

CREATE INDEX idx_knowledge_base_category
    ON knowledge_base(category);

CREATE INDEX idx_knowledge_base_created_at
    ON knowledge_base(created_at);

CREATE INDEX idx_knowledge_base_deleted
    ON knowledge_base(deleted);


-- =========================================================
-- 4. knowledge_base_member 索引
-- =========================================================

CREATE INDEX idx_kb_member_knowledge_base_id
    ON knowledge_base_member(knowledge_base_id);

CREATE INDEX idx_kb_member_user_id
    ON knowledge_base_member(user_id);

CREATE INDEX idx_kb_member_role
    ON knowledge_base_member(member_role);

CREATE INDEX idx_kb_member_deleted
    ON knowledge_base_member(deleted);


--创建文档数据库
CREATE TABLE knowledge_document
(
    id                BIGSERIAL PRIMARY KEY,
    knowledge_base_id BIGINT       NOT NULL,
    name              VARCHAR(255) NOT NULL,
    original_name     VARCHAR(255) NOT NULL,
    file_type         VARCHAR(32)  NOT NULL,
    file_size         BIGINT       NOT NULL,
    storage_path      VARCHAR(500) NOT NULL,
    status            VARCHAR(32)  NOT NULL DEFAULT 'UPLOADED',
    version           INTEGER      NOT NULL DEFAULT 1,
    uploaded_by       BIGINT       NOT NULL,
    error_message     VARCHAR(500),
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted           BOOLEAN      NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_document_knowledge_base
        FOREIGN KEY (knowledge_base_id)
            REFERENCES knowledge_base (id),

    CONSTRAINT fk_document_uploaded_by
        FOREIGN KEY (uploaded_by)
            REFERENCES sys_user (id),

    CONSTRAINT ck_document_file_type
        CHECK (file_type IN ('TXT', 'MD')),

    CONSTRAINT ck_document_file_size
        CHECK (file_size > 0),

    CONSTRAINT ck_document_status
        CHECK (status IN ('UPLOADED', 'PROCESSING', 'READY', 'FAILED')),

    CONSTRAINT ck_document_version
        CHECK (version > 0)
);

CREATE INDEX idx_document_knowledge_base_id
    ON knowledge_document (knowledge_base_id);

CREATE INDEX idx_document_uploaded_by
    ON knowledge_document (uploaded_by);

CREATE INDEX idx_document_status
    ON knowledge_document (status);

CREATE INDEX idx_document_file_type
    ON knowledge_document (file_type);

CREATE INDEX idx_document_created_at
    ON knowledge_document (created_at);

CREATE INDEX idx_document_deleted
    ON knowledge_document (deleted);

COMMENT ON TABLE knowledge_document IS '知识库文档表';


CREATE TABLE document_chunk
(
    id                 BIGSERIAL PRIMARY KEY,

    document_id        BIGINT NOT NULL,
    knowledge_base_id  BIGINT NOT NULL,

    chunk_index        INTEGER NOT NULL,
    content            TEXT NOT NULL,
    char_count         INTEGER NOT NULL,

    section_title      VARCHAR(500),
    section_level      INTEGER,

    metadata           JSONB NOT NULL DEFAULT '{}'::jsonb,

    document_version   INTEGER NOT NULL DEFAULT 1,

    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    deleted            BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_chunk_document
        FOREIGN KEY (document_id)
            REFERENCES knowledge_document(id),

    CONSTRAINT fk_chunk_knowledge_base
        FOREIGN KEY (knowledge_base_id)
            REFERENCES knowledge_base(id),

    CONSTRAINT ck_chunk_index
        CHECK (chunk_index >= 0),

    CONSTRAINT ck_chunk_char_count
        CHECK (char_count > 0),

    CONSTRAINT ck_chunk_section_level
        CHECK (
            section_level IS NULL
                OR section_level BETWEEN 1 AND 6
            ),

    CONSTRAINT ck_chunk_document_version
        CHECK (document_version > 0),

    CONSTRAINT uk_document_chunk_index
        UNIQUE (
                document_id,
                document_version,
                chunk_index
            )
);


CREATE INDEX idx_document_chunk_document_id
    ON document_chunk(document_id);

CREATE INDEX idx_document_chunk_knowledge_base_id
    ON document_chunk(knowledge_base_id);

CREATE INDEX idx_document_chunk_document_version
    ON document_chunk(document_id, document_version);

CREATE INDEX idx_document_chunk_deleted
    ON document_chunk(deleted);
