package com.ic.filemanage.controller;

import com.ic.filemanage.common.Result;
import com.ic.filemanage.model.UserEntity;
import com.ic.filemanage.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    private LoginService loginService;


    @PostMapping ("/login")
    public Result<String> userLogin(@RequestParam String userName, @RequestParam String password) {
        String token = loginService.userLogin(userName, password);
        return Result.success2(token);
    }


    @PostMapping("/logOut")
    public Result<Void> logOutAccount(){
        return Result.success1();
    }//前端把token删了

    @PutMapping("/edit_password/{id}")
    public Result<Void> editPassword(@RequestAttribute("loginUser") UserEntity currentUser,
                                     @RequestParam String oldPassword,
                                     @RequestParam String newPassword) {
        loginService.editPassword(currentUser, oldPassword, newPassword);
        return Result.success1();
    }
}

