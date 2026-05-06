package com.ic.filemanage.common;

import com.alibaba.fastjson.JSON;
import com.ic.filemanage.model.SysLogEntity;
import com.ic.filemanage.model.UserEntity;
import com.ic.filemanage.repository.SysLogRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Aspect
@Component
public class LogAspect {
    @Autowired
    private SysLogRepository sysLogRepository;

    // 拦截操作成功的请求
    @AfterReturning(pointcut = "@annotation(autoLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, AutoLog autoLog, Object jsonResult) {
        handleLog(joinPoint, autoLog, null);
    }

    // 拦截发生异常的请求（记录失败日志）
    @AfterThrowing(value = "@annotation(autoLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, AutoLog autoLog, Exception e) {
        handleLog(joinPoint, autoLog, e);
    }

    protected void handleLog(final JoinPoint joinPoint, AutoLog autoLog, final Exception e) {
        try {
            SysLogEntity log = new SysLogEntity();
            log.setCreateTime(LocalDateTime.now());
            log.setOperationType(autoLog.value());
            log.setModule(autoLog.module());

            // 1. 获取 IP
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            log.setOperatorIp(getIpAddr(request));

            // 2. 获取当前操作人 (从参数里找 UserEntity)
            Object[] args = joinPoint.getArgs();
            UserEntity currentUser = null;
            for (Object arg : args) {
                if (arg instanceof UserEntity) {
                    currentUser = (UserEntity) arg;
                    break;
                }
            }
            if (currentUser != null) {
                log.setOperatorId(currentUser.getUserId());
                log.setOperatorName(currentUser.getName());
            } else {
                log.setOperatorName("未知/系统");
            }

            if (args.length > 0) {
                try {
                    String params = JSON.toJSONString(args[0]);
                    log.setParams(params.length() > 500 ? params.substring(0, 500) : params);
                } catch (Exception ex) {
                    // 序列化失败忽略
                }
            }

            // 4. 状态
            if (e != null) {
                log.setStatus(0);
                log.setErrorMsg(e.getMessage().length() > 200 ? e.getMessage().substring(0, 200) : e.getMessage());
            } else {
                log.setStatus(1);
            }

            sysLogRepository.save(log);

        } catch (Exception exp) {
            // 记录日志本身报错不能影响业务
            exp.printStackTrace();
        }
    }

    // 获取真实IP工具方法
    private String getIpAddr(HttpServletRequest request) {
        if (request == null) return "unknown";
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

