package com.ic.filemanage.modelDTO.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class UserCreateReq {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 6, max = 20, message = "用户名长度必须在6到20个字符之间")
    private String username;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "手机和不能为空")
    private String phoneNumber;  // 手机号用String

    @NotBlank(message = "部门不能为空")
    private String departmentName;

    @NotNull(message = "角色不能为空")
    private Integer role; // Integer 必须用 NotNull

    @NotNull(message = "状态不能为空")
    private Integer status; // Integer 必须用 NotNull
}
