package com.newscp.backend.sys.role.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("nscp_sys_permission")
public class SysPermission {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String permType;
    private String permCode;
    private String permName;
    private String routePath;
    private String componentPath;
    private String icon;
    private Integer sortOrder;
    private Integer isHidden;
    private String status;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
