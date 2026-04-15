# 将 schema/ 与 seed/ 合并为 stage1_init.sql、stage2_init.sql（在 db 目录下执行：.\merge_stage_sql.ps1）
$ErrorActionPreference = "Stop"
$db = $PSScriptRoot
$h1 = @"
-- =============================================================================
-- 阶段1（合并入口）：身份/组织表结构 + 菜单种子 + 管理员种子
-- 维护：只改 schema/ 与 seed/，再运行 merge_stage_sql.ps1 重新生成本文件。
-- 等价于：01_nscp_sys_identity + 02_nscp_sys_organization + 10_seed_permissions_menu + 20_seed_roles_users
-- =============================================================================

"@
$h2 = @"
-- =============================================================================
-- 阶段2（合并入口）：租户表结构 + 平台租户/角色种子
-- 等价于：03_nscp_sys_tenant + 30_seed_tenant_platform
-- =============================================================================

"@
$stage1 = $h1 + (Get-Content -Raw "$db\schema\01_nscp_sys_identity.sql") + "`n`n" + (Get-Content -Raw "$db\schema\02_nscp_sys_organization.sql") + "`n`n" + (Get-Content -Raw "$db\seed\10_seed_permissions_menu.sql") + "`n`n" + (Get-Content -Raw "$db\seed\20_seed_roles_users.sql")
$stage2 = $h2 + (Get-Content -Raw "$db\schema\03_nscp_sys_tenant.sql") + "`n`n" + (Get-Content -Raw "$db\seed\30_seed_tenant_platform.sql")
Set-Content -Path "$db\stage1_init.sql" -Value $stage1 -Encoding utf8
Set-Content -Path "$db\stage2_init.sql" -Value $stage2 -Encoding utf8
Write-Host "OK: stage1_init.sql, stage2_init.sql"
