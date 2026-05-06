package com.ic.filemanage.modelDTO.req;

import com.ic.filemanage.model.DepartmentEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class DeptCreateReq {
    private String deptName;
    private String parentName;
    private Integer orderNum;
}
