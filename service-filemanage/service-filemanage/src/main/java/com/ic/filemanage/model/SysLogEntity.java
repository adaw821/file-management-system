package com.ic.filemanage.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sys_log")
public class SysLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 操作人信息
    private Long operatorId;
    private String operatorName;
    private String operatorIp;

    // 操作内容
    private String operationType; // 例如：新增用户、删除文件
    private String module;        // 例如：用户模块、文件模块

    // 操作对象（关键信息）
    private String targetId;      // 被操作对象的ID (文件ID/用户ID)
    private String targetName;    // 被操作对象的名称

    // 详细参数（JSON格式，备用）
    @Column(columnDefinition = "TEXT")
    private String params;

    // 结果
    private Integer status;       // 1 成功 0 失败
    private String errorMsg;

    private LocalDateTime createTime;
}