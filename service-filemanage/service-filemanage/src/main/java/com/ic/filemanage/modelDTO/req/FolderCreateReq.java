package com.ic.filemanage.modelDTO.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.time.LocalDateTime;

@Data  // Lombok自动生成getter/setter
@NoArgsConstructor  // 无参构造器
@AllArgsConstructor // 全参构造器
public class FolderCreateReq {
    private String foldName;
    private String parentName;// 0表示根目录
    private String creatorName;
    private Integer authType;;//0私有， 1部门可见， 2全员可见
    private LocalDateTime createTime = LocalDateTime.now();

}
