package com.ic.filemanage.controller;

import com.ic.filemanage.common.PageResult;
import com.ic.filemanage.common.Result;
import com.ic.filemanage.model.SysLogEntity;
import com.ic.filemanage.model.UserEntity;
import com.ic.filemanage.service.SysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/log")
public class SysLogController {
    @Autowired
    private SysLogService sysLogService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String operator,
                       @RequestParam(required = false) String type,
                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
                       @RequestAttribute("loginUser") UserEntity currentUser) {

        // 权限控制：只有管理员能看日志
        if (currentUser.getRole() != 1) {
            return Result.error("无权访问");
        }

        PageResult<SysLogEntity> result = sysLogService.searchLogs(operator, type, startTime, endTime, page, size);
        return Result.success2(result);
    }
}
