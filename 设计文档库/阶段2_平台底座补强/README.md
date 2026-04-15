# 阶段 2：平台与安全底座补强

> 更新时间：2026-04-15  
> 权威计划：见仓库根目录 [`重构计划.md`](../../重构计划.md) 第四章阶段 2。

## 1. 子项完成度（截至 2026-04-15）

| 子项 | 状态 | 说明 |
|------|------|------|
| 2.1 租户与开通（P0） | ✅ 已完成 | 租户 CRUD、用户-租户关联、默认租户、超管租户 `admin` |
| 2.2 菜单与权限运营（P0） | ✅ 已完成 | 权限树在线运营 API + 页面、与角色授权联动 |
| 2.3 数据权限（P1） | ⏳ 未开始 | 规则模型/API/UI 骨架未落地 |
| 2.4 密码与安全策略（P1） | 🟡 部分完成 | 已有登录失败锁定；策略配置化未完成 |
| 2.5 审计与日志（P1） | ⏳ 未开始 | 登录日志与关键操作审计未落地 |
| 2.6 认证加固（P2） | ⏳ 未开始 | Refresh Token/验证码/OAuth/SSO 未落地 |
| 2.7 前端枚举值映射治理（P0） | 🟡 进行中 | 规则已补充；统一 `constants` 映射层未完成 |

## 2. 已落地代码位置（便于排查）

### 2.1 租户与开通

- 后端：
  - `backend/src/main/java/com/newscp/backend/tenant/`
  - `backend/src/main/resources/db/stage2_init.sql`
- 前端：
  - `frontend/src/pages/system/tenants/index.tsx`
  - `frontend/src/api/system.ts`（`/api/sys/tenants` 相关）

### 2.2 菜单与权限运营

- 后端：
  - `backend/src/main/java/com/newscp/backend/sys/permission/`
  - `backend/src/main/resources/db/stage2_init.sql`（权限种子）
- 前端：
  - `frontend/src/pages/system/permissions/index.tsx`
  - `frontend/src/router/routeComponentMap.tsx`

### 2.4 / 2.6 相关（当前仅部分能力）

- `backend/src/main/java/com/newscp/backend/auth/AuthService.java`
- `backend/src/main/java/com/newscp/backend/auth/jwt/JwtAuthenticationFilter.java`
- `backend/src/main/java/com/newscp/backend/tenant/TenantContext.java`

## 3. 下一步文档拆分建议

建议后续按以下文件拆分，避免本 README 过重：

- `2.1_租户与开通.md`
- `2.2_菜单与权限运营.md`
- `2.3_数据权限模型.md`
- `2.4_密码与安全策略.md`
- `2.5_审计与日志.md`
- `2.6_认证加固.md`
- `2.7_前端枚举映射治理.md`
