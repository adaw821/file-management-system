package com.ic.filemanage.modelDTO.req;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class UserLoginReq {
    @NotBlank(message = "用户名不为空")
    private String userName;

    @NotBlank(message = "密码不为空")
    private String password;
}
