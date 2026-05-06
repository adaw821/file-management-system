package com.ic.filemanage.modelDTO.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResp {
    private Long userId;
    private String username;
    private String name;
    private String departmentName;
    private String phoneNumber;
    private Integer status;
    private LocalDateTime createTime = LocalDateTime.now();
}
