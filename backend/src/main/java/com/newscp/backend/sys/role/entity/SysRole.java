package com.newscp.backend.sys.role.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("nscp_sys_role")
public class SysRole {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String tenantId;
    private String roleCode;
    private String roleName;
    private String remark;
    private Integer sortOrder;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
