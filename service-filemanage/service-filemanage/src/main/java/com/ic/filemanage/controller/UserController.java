package com.ic.filemanage.controller;

import com.ic.filemanage.common.PageResult;
import com.ic.filemanage.common.Result;
import com.ic.filemanage.model.UserEntity;
import com.ic.filemanage.modelDTO.req.UserCreateReq;
import com.ic.filemanage.modelDTO.req.UserEditReq;
import com.ic.filemanage.modelDTO.req.UserSearchReq;
import com.ic.filemanage.modelDTO.resp.UserResp;
import com.ic.filemanage.repository.UserRepository; // 👈 记得导包
import com.ic.filemanage.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @DeleteMapping("/delete")
    public Result<Void> deleteUser(@RequestParam Long userId, @RequestAttribute("loginUser") UserEntity currentUser){
        userService.deleteUser(userId, currentUser);
        return Result.success1();
    }

    // 创建用户
    @PostMapping("/create")
    public Result<Void> createNewUser(
            @Valid @RequestBody UserCreateReq req,
            @RequestAttribute("loginUser") UserEntity currentUser
    ) {
        try {
            if(currentUser.getRole() != 1) { // 检查权限
                throw new RuntimeException("没有权限");
            }
            userService.createUser(req);
            return Result.success1();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // 修改用户
    @PutMapping("/edit")
    public Result<Void> editUser(
            @Valid @RequestBody UserEditReq req,
            @RequestAttribute("loginUser") UserEntity currentUser,
            @RequestParam String username
    ) {
        try {
            if(currentUser.getRole() != 1) { // 检查权限
                throw new RuntimeException("没有权限");
            }
            userService.editUser(req, username);
            return Result.success1();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // 搜索
    @PostMapping("/search")
    public Result<PageResult<UserResp>> searchUsers(@RequestBody UserSearchReq req) {
        PageResult<UserResp> resps = userService.searchUsers(req);
        return Result.success2(resps);
    }

    // 离职
    @PutMapping("/resign")
    public Result<Void> setResign(
            @RequestParam String username,
            @RequestAttribute("loginUser") UserEntity currentUser
    ) {
        try {
            if(currentUser.getRole() != 1) { // 检查权限
                throw new RuntimeException("没有权限");
            }
            userService.setResign(username);
            return Result.success1();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // 重置密码
    @PutMapping("/reset")
    public Result<Void> resetPassword(
            @RequestParam String username,
            @RequestAttribute("loginUser") UserEntity currentUser
    ) {
        try {
            if(currentUser.getRole() != 1) { // 检查权限
                throw new RuntimeException("没有权限");
            }
            userService.resetPassword(username);
            return Result.success1();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}


