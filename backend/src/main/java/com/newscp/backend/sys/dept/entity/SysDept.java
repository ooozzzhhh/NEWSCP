package com.newscp.backend.sys.dept.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("nscp_sys_dept")
public class SysDept {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String tenantId;
    private String deptName;
    private Long parentId;
    private Long leaderId;
    private Integer sortOrder;
    private String remark;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
