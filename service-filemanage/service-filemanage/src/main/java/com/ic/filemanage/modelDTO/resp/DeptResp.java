package com.ic.filemanage.modelDTO.resp;

import com.ic.filemanage.model.DepartmentEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class DeptResp {
    private String deptName;
    private String parentName;
    private int userNum;
    private LocalDateTime createTime = LocalDateTime.now();
}
