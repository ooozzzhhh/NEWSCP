package com.newscp.backend.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newscp.backend.tenant.entity.SysTenant;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysTenantMapper extends BaseMapper<SysTenant> {

    @Select({
            "<script>",
            "SELECT tenant_id",
            "FROM nscp_sys_tenant",
            "WHERE deleted = 0",
            "AND status = 'ENABLED'",
            "<if test='tenantIds != null and tenantIds.size() > 0'>",
            "AND tenant_id IN",
            "<foreach item='id' collection='tenantIds' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</if>",
            "</script>"
    })
    List<String> selectEnabledTenantIds(@Param("tenantIds") List<String> tenantIds);
}
