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