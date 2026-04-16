# 阶段 2：平台与安全底座补强

> 更新时间：2026-04-16  
> 总计划与全量子项状态：见仓库根目录 [`重构计划.md`](../../重构计划.md) 第四章阶段 2。

## 1. 本目录设计文档（仅已落地能力）

| 文档 | 对应子项 | 说明 |
|------|----------|------|
| [2.1 租户与开通](2.1_租户与开通.md) | 2.1 | 租户 CRUD、用户-租户绑定、登录校验、`X-Tenant-Id` 运行时租户 |
| [2.2 菜单与权限运营](2.2_菜单与权限运营.md) | 2.2 | 权限树在线维护 API + 页面，与角色授权联动 |
| [2.4 密码与安全策略](2.4_密码与安全策略.md) | 2.4 | 与总计划同名；§1 区分已实现/未实现 |

其余子项（数据权限、审计、认证加固、枚举治理统一出口、ORM 迁移等）以《重构计划》为准，**落地后再补设计文档**。

## 2. 代码落点速查

**租户：** `backend/.../tenant/` · `frontend/src/pages/system/tenants/` · `db/stage2_init.sql`

**权限运营：** `backend/.../sys/permission/` · `frontend/src/pages/system/permissions/` · `frontend/src/router/routeComponentMap.tsx`

**2.4 密码与安全策略（已实现部分）：** `backend/.../auth/AuthService.java` · `SysUserMapper`（登录失败锁定）

**租户请求头：** `backend/.../auth/jwt/JwtAuthenticationFilter.java` · `frontend/src/api/client.ts`
