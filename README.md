# campus-proposal · 校园事务提案督办平台

一个"小而完整"的全栈演示项目:学生提交校园事务提案,提案工作办公室审查立案并分办到承办部门,承办部门答复后由提案人评价办结,**全过程留痕**。

业务主线:`提交 → 审查立案 → 分办 → 答复 → 评价办结`(另含"不予立案"分支)。

## 技术栈

| 层 | 选型 | 说明 |
|---|---|---|
| 后端 | Java 17 + Spring Boot 3.5 | Web / Data JPA / Validation |
| 数据库 | PostgreSQL | 兼容人大金仓、openGauss 等 PG 系信创数据库 |
| 前端 | 原生 HTML/CSS/JS 单页 | 零 CDN、零 npm 依赖,离线可演示 |
| 认证 | HttpSession + 拦截器 | 演示级实现,生产应换 Spring Security + BCrypt |

## 快速启动

```bash
# 1. 准备数据库(本机 PostgreSQL 已建好可跳过)
createdb campus_proposal   # 或 psql -c "CREATE DATABASE campus_proposal ENCODING 'UTF8'"

# 2. 配置数据库密码(环境变量,或改 application.yml 默认值)
set DB_PASSWORD=你的密码        # Windows
export DB_PASSWORD=你的密码     # Linux/macOS

# 3. 启动(Spring Boot 会自动建表并写入演示数据)
mvn spring-boot:run

# 4. 打开 http://localhost:8080
```

演示账号(密码均为 `123456`,前端登录页可一键填入):

| 账号 | 姓名 | 角色 |
|---|---|---|
| xiaoming / xiaohong | 张晓宇 / 陈雨桐 | 学生提案人 |
| staff | 李静(提案工作办公室) | 工作人员:审查、分办 |
| houqin / jiaowu | 王建国(后勤管理处)/ 刘敏(教务处) | 承办部门:答复 |

首次启动自动写入 5 条处于不同阶段的提案,方便直接演示各状态。

## 功能与接口

- `POST /api/auth/login|logout`、`GET /api/auth/me` —— 会话登录
- `GET /api/proposals[?status=]` —— 列表(**角色感知**:提案人只见自己的)
- `GET /api/proposals/{id}` —— 详情 + 办理留痕时间线
- `POST /api/proposals` —— 提交(仅学生)
- `POST /api/proposals/{id}/review` —— 立案 / 退回(仅提案办)
- `POST /api/proposals/{id}/assign` —— 分办(仅提案办)
- `POST /api/proposals/{id}/reply` —— 答复(仅被分办承办部门)
- `POST /api/proposals/{id}/evaluate` —— 评价办结(仅提案人本人)
- `GET /api/stats` —— 总览统计(口径与列表一致)
- `GET /api/users?role=DEPT` —— 承办部门下拉(仅提案办)

统一异常返回 `{code, message}`;参数校验失败返回第一条字段错误。

## 架构要点

```
controller  ── 只做参数接收/角色校验入口/组装返回
service     ── 业务规则;状态变更唯一入口 transition():校验状态机 → 改状态 → 写留痕(@Transactional)
repository  ── Spring Data JPA
entity      ── User / Proposal / ProposalLog(留痕只增不改)
common      ── Role、ProposalStatus(状态机:合法流转表 + canTransitionTo)
```

**为什么所有状态变更必须走同一个 `transition()` 漏斗**:流程完整性的关键防线——任何接口都绕不过"先校验状态机再落库+留痕",从代码结构上杜绝了"跳步骤、办结后还能改"这类脏数据。配套 `ProposalStatusTest` 单测锁定状态机规则。

## 已知简化(生产化方向)

- 密码明文比对 → 生产应 BCrypt 哈希 + Spring Security
- 提案编号 `TA-<year>-<seq>` 按"数量+1"生成,并发可能撞号 → 应改数据库序列
- `ddl-auto: update` 自动建表 → 生产应 Flyway/Liquibase 迁移脚本
- 会话认证 → 生产应 JWT/OAuth2,前端可换 Vue/React

