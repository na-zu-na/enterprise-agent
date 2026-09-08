# Enterprise Agent

面向企业内部场景的智能助手后端。项目提供知识库与文档管理、RAG 问答、Agent 对话与审批、个人待办任务等能力。

## 技术栈

- Java 17、Spring Boot 4.1
- Spring MVC、Spring Validation、Spring Data Redis
- MyBatis-Plus、PostgreSQL
- Elasticsearch（文档分块索引与检索）
- JWT 鉴权
- 外部 AI 服务（默认 `http://localhost:8000`）

## 功能概览

| 模块 | 能力 |
| --- | --- |
| 认证与用户 | 登录、JWT 鉴权、用户搜索、登录日志 |
| 知识库 | 创建、查询、编辑、删除知识库；成员与角色管理 |
| 文档 | 上传、列表、详情、重命名、下载、预览、解析与删除 |
| 智能检索 | 基于可访问知识库的 RAG 问答 |
| Agent | 多轮对话、会话记录、工具审批恢复；调用 AI 服务时转发用户 ID |
| 个人任务 | 创建、查询、编辑、完成与删除当前登录用户的任务 |

## 前置条件

- JDK 17+
- Maven 3.6.3+
- PostgreSQL
- Elasticsearch（默认 `http://localhost:9200`）
- 与本项目配套的 AI 服务（默认 `http://localhost:8000`）

> 文档解析与 Agent 对话依赖 AI 服务。文档解析服务目前对应 `.txt`、`.md` 和 `.markdown` 格式。

## 本地启动

1. 创建 PostgreSQL 数据库。

   ```sql
   CREATE DATABASE enterprise_agent;
   ```

2. 导入初始化表结构。

   ```powershell
   psql -U postgres -d enterprise_agent -f src/main/resources/static/db/enterprise_agent.sql
   ```

3. 修改 `src/main/resources/application.yml` 中的数据库、JWT 与 Elasticsearch 配置。生产环境请不要提交真实密码或 JWT 密钥。

4. 启动 PostgreSQL、Elasticsearch 以及 AI 服务。

5. 启动 Spring Boot 应用。

   ```powershell
   mvn spring-boot:run
   ```

默认端口为 `8080`，API 根路径为 `http://localhost:8080/api`。

## 配置说明

| 配置项 | 说明 | 默认值 |
| --- | --- | --- |
| `spring.datasource.*` | PostgreSQL 连接信息 | `enterprise_agent` 数据库 |
| `spring.elasticsearch.uris` | Elasticsearch 地址 | `http://localhost:9200` |
| `jwt.secret` | JWT 签名密钥 | 必须替换为安全密钥 |
| `jwt.expiration` | JWT 过期时间（毫秒） | `7200000` |
| `ai.service.base-url` | AI 服务地址 | `http://localhost:8000` |
| `ai.service.connect-timeout` | 连接 AI 服务的最长等待时间 | `10s` |
| `ai.service.read-timeout` | AI 解析/推理响应的最长等待时间 | `5m` |
| `document.storage-root` | 上传文档的本地存储根目录 | `src/main/resources/static` |

## 鉴权与统一响应

除 `POST /api/auth/login` 外，`/api/**` 请求都需要携带 JWT：

```http
Authorization: Bearer <token>
```

成功响应结构：

```json
{
  "data": {},
  "message": "success",
  "code": 200
}
```

登录后，后端会从 JWT 中获取当前用户。调用 AI 服务的 `/agent/chat` 和 `/agent/approvals/respond` 时，还会自动转发：

```http
X-Authenticated-User-Id: <当前用户 ID>
```

## API 速查

### 认证与用户

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/auth/login` | 登录并获取 JWT |
| `GET` | `/api/users/search?keyword={keyword}` | 搜索用户 |

登录请求示例：

```json
{
  "username": "alice",
  "password": "password"
}
```

### 知识库与文档

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/knowledge-bases` | 创建知识库 |
| `GET` | `/api/knowledge-bases` | 查询有权访问的知识库 |
| `GET` | `/api/knowledge-bases/accessible-ids` | 查询可访问知识库 ID |
| `GET` | `/api/knowledge-bases/{id}` | 查询知识库详情 |
| `PUT` | `/api/knowledge-bases/{id}` | 更新知识库 |
| `DELETE` | `/api/knowledge-bases/{id}` | 删除知识库 |
| `POST` | `/api/knowledge-bases/{id}/documents` | 上传文档（`multipart/form-data`，字段名 `file`） |
| `GET` | `/api/knowledge-bases/{id}/documents` | 分页查询文档 |
| `GET` | `/api/documents/{id}` | 查询文档详情 |
| `PUT` | `/api/documents/{id}` | 重命名文档 |
| `DELETE` | `/api/documents/{id}` | 删除文档 |
| `GET` | `/api/documents/{id}/download` | 下载文档 |
| `GET` | `/api/documents/{id}/preview` | 预览文档 |
| `POST` | `/api/documents/{id}/parse` | 调用 AI 服务解析并切分文档 |
| `POST` | `/api/documents/rag/query` | 在可访问知识库中执行 RAG 问答 |

知识库成员管理：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/knowledge-bases/{id}/members` | 查询成员 |
| `POST` | `/api/knowledge-bases/{id}/members` | 添加成员 |
| `PUT` | `/api/knowledge-bases/{id}/members/{userId}` | 更新成员角色 |
| `DELETE` | `/api/knowledge-bases/{id}/members/{userId}` | 移除成员 |

知识库分类为 `GENERAL`、`POLICY`、`PROJECT` 或 `TECHNICAL`；可见范围为 `PRIVATE`、`MEMBER` 或 `PUBLIC`；成员角色为 `VIEWER`、`EDITOR` 或 `ADMIN`。

### Agent

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/chat` | 发起或续接 Agent 对话 |
| `GET` | `/api/agent/conversations` | 获取当前用户的会话列表 |
| `GET` | `/api/agent/conversations/{conversationId}/messages` | 获取会话消息 |
| `DELETE` | `/api/agent/conversations/{conversationId}` | 删除会话 |
| `POST` | `/api/agent/approvals/respond` | 提交工具调用审批结果 |

发起对话：

```json
{
  "query": "请总结公司的年假政策",
  "knowledgeBaseIds": [1, 2],
  "conversationId": null
}
```

`knowledgeBaseIds` 可省略，此时服务会使用当前用户所有可访问的知识库。续接会话时传入已有的 `conversationId`。

### 个人任务

任务接口由 [`UserTaskController`](src/main/java/org/cc/enterpriseagent/task/controller/UserTaskController.java) 提供。所有操作都仅作用于当前 JWT 对应的用户；不需要，也不能通过请求体指定 `userId`。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/tasks` | 创建任务，初始状态固定为 `TODO` |
| `GET` | `/api/tasks` | 查询我的任务，按截止时间升序、创建时间降序排列 |
| `PUT` | `/api/tasks/{id}` | 更新标题、描述和截止时间 |
| `PUT` | `/api/tasks/{id}/status` | 更新任务状态 |
| `DELETE` | `/api/tasks/{id}` | 软删除任务 |

创建任务：

```http
POST /api/tasks
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "完成项目周报",
  "description": "汇总本周进度和风险项",
  "dueAt": "2026-09-10T18:00:00"
}
```

更新任务状态：

```http
PUT /api/tasks/1/status
Authorization: Bearer <token>
Content-Type: application/json

{
  "status": "DONE"
}
```

任务状态仅支持 `TODO` 和 `DONE`；任务标题不能为空，最长 255 个字符。删除使用 MyBatis-Plus 逻辑删除，不会物理移除数据库记录。

## 目录结构

```text
src/main/java/org/cc/enterpriseagent
├── agent/           # Agent 对话、会话与审批
├── auth/            # 登录与登录日志
├── common/          # JWT、拦截器、通用响应与配置
├── document/        # 文档、分块、RAG 与 Elasticsearch 同步
├── knowledgebase/   # 知识库、成员与本地文件存储
├── task/            # 用户个人任务
└── user/            # 用户查询

src/main/resources
├── application.yml
└── static/db/enterprise_agent.sql
```

## 开发提示

- 上传成功不等于文档解析完成；解析接口会同步调用 AI 服务并写入文档分块。
- AI 服务首次加载向量模型时可能耗时较长，Java 端默认最长等待 5 分钟。
- 上传的文档默认保存在 `src/main/resources/static/doc`。部署时建议将其改为应用外部、可持久化的目录。
- 请在部署前通过环境变量、密钥管理服务或未纳入版本控制的配置文件注入数据库密码与 JWT 密钥。
