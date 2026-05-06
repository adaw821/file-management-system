package com.ic.filemanage.utils;

import com.ic.filemanage.model.UserEntity;
import com.ic.filemanage.repository.LoginRepository;
import com.ic.filemanage.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private LoginRepository loginRepository;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");

        if (token == null || token.isEmpty()) {
            throw new RuntimeException("未登录");
        }

        try {
            Claims claims = jwtUtils.parseToken(token);

            Long userId = Long.valueOf(claims.get("userId").toString());
            UserEntity user = loginRepository.findById(userId).orElse(null);

            if (user == null || user.getStatus() == 0){
                throw new RuntimeException("用户已离职");
            }

            request.setAttribute("loginUser", user);
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            response.getWriter().write("Invalid token.");
            return false;
       }
    }
}
