package com.newscp.backend.sys.security.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newscp.backend.sys.security.entity.PasswordPolicy;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PasswordPolicyMapper extends BaseMapper<PasswordPolicy> {
}
