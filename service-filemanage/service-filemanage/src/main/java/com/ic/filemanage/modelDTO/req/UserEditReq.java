package com.ic.filemanage.modelDTO.req;

import lombok.Data;

@Data
public class UserEditReq {
    private String name;
    private String phoneNumber;
    private String departmentName;
    private Integer role;
    private Integer status;
}
