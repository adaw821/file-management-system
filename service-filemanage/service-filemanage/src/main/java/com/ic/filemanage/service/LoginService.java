package com.ic.filemanage.service;

import com.ic.filemanage.common.AutoLog;
import com.ic.filemanage.model.UserEntity;
import com.ic.filemanage.repository.LoginRepository;
import com.ic.filemanage.utils.JwtUtils;
import com.ic.filemanage.utils.Md5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private JwtUtils jwtUtils;

    public String userLogin(String username, String password) {
        UserEntity user;
        user = loginRepository.findByUsername(username);

        if(user == null){
            throw new RuntimeException("用户名错误重试");
        }

        String md5Password = Md5Utils.code(password);
        if(password == null || !user.getPassword().equals(md5Password)){
            throw new RuntimeException("密码或者用户名错误重试");
        }

        if(user.getStatus() == 0){
            throw new RuntimeException("离职用户不可以登录");
        }

        String token = jwtUtils.createToken(user.getUserId(), username);
        return token;
    }



    @AutoLog(value = "修改个人密码", module = "个人中心")
    public void editPassword(UserEntity currentUser, String oldPassword, String newPassword) {
        if(currentUser == null){
            throw new RuntimeException("用户不存在");
        }

        if(!currentUser.getPassword().equals(Md5Utils.code(oldPassword))){
            throw new RuntimeException("原密码输入错误");
        }

        if(newPassword.length() < 6){
            throw new RuntimeException("新密码长度不够");
        }

        currentUser.setPassword(Md5Utils.code(newPassword));
        loginRepository.save(currentUser);
    }

}
