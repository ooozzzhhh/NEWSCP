package com.newscp.backend.sys.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newscp.backend.sys.dict.entity.DictItem;
import com.newscp.backend.sys.dict.vo.DictDropdownVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DictItemMapper extends BaseMapper<DictItem> {

    @Select({
            "<script>",
            "SELECT type_code AS typeCode, value, label, color, is_default AS isDefault, sort_order AS sortOrder",
            "FROM nscp_sys_dict_item",
            "WHERE tenant_id = #{tenantId}",
            "AND deleted = 0",
            "AND status = 1",
            "<if test='typeCodes != null and typeCodes.size() > 0'>",
            "AND type_code IN",
            "<foreach item='code' collection='typeCodes' open='(' separator=',' close=')'>",
            "#{code}",
            "</foreach>",
            "</if>",
            "ORDER BY type_code ASC, sort_order ASC, id ASC",
            "</script>"
    })
    List<DictDropdownVO> selectEnabledDropdown(
            @Param("tenantId") String tenantId,
            @Param("typeCodes") List<String> typeCodes
    );
}
