package com.ic.filemanage.modelDTO.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data  // Lombok自动生成getter/setter
@NoArgsConstructor  // 无参构造器
@AllArgsConstructor // 全参构造器
public class FolderResp {
    private String folderName;
    private Long creatorId; // 创建人ID
    private LocalDateTime createTime;
    private int size;
}
