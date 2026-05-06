package com.ic.filemanage.modelDTO.resp;

import lombok.Data;

import java.time.LocalDateTime;

@Data
//文件列表展示	展示字段：原文件名、文件类型、大小（MB）、上传人、上传时间、下载次数，支持分页（默认 10 条 / 页）；仅显示有权访问的文件
public class FileResp {
    private String type;
    private String fileName;
    private Long FileSize;
    private LocalDateTime createTime;
    private String ownerName;  // 创建人ID
    private Integer times;

    private int page = 1;
    private int size = 10;
}
