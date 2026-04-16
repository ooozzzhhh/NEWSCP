package com.newscp.backend.sys.dict.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("nscp_sys_dict_type")
public class DictType {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String tenantId;
    private String typeCode;
    private String typeName;
    private String source;
    private Integer editable;
    private Integer status;
    private Integer sortOrder;
    private String remark;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
