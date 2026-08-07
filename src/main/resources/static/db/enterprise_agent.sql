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