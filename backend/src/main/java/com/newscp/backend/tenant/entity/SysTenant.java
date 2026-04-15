package com.newscp.backend.tenant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("nscp_sys_tenant")
public class SysTenant {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String tenantId;
    private String tenantName;
    private String status;
    private LocalDateTime expireAt;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String remark;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
