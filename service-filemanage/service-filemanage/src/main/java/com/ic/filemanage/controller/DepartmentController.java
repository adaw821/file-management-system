package com.ic.filemanage.controller;

import com.ic.filemanage.common.Result;
import com.ic.filemanage.model.DepartmentEntity;
import com.ic.filemanage.model.UserEntity;
import com.ic.filemanage.modelDTO.req.DeptCreateReq;
import com.ic.filemanage.modelDTO.resp.DeptResp;
import com.ic.filemanage.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dept")
public class DepartmentController {
    @Autowired
    private DeptService deptService;

    @PostMapping(value = "/create")
    public Result<Void> createDepartment(@RequestBody DeptCreateReq dept, @RequestAttribute("loginUser") UserEntity currentUser) {
        if(currentUser.getRole() != 1){
            throw new RuntimeException("只有管理员可以登录");
        }
        deptService.createDept(dept, currentUser);
        return Result.success1();
    }

    @PutMapping(value = "/update/{id}")
    public Result<Void> updateDepartment(@PathVariable Long id, @RequestParam String newDeptName,
                                         @RequestParam Long parentId,
                                         @RequestParam Integer orderNum,
                                         @RequestAttribute("loginUser") UserEntity currentUser) {
        if(currentUser.getRole() != 1){
            throw new RuntimeException("只有管理员可以登录");
        }
        deptService.updateDept(id, newDeptName, parentId, orderNum);
        return Result.success1();
    }

    @DeleteMapping(value = "/delete/{id}")
    public Result<Void> deleteDepartment(@PathVariable Long id, @RequestAttribute("loginUser") UserEntity currentUser) {
        if(currentUser.getRole() != 1){
            throw new RuntimeException("只有管理员可以登录");
        }
        deptService.deleteDept(id);
        return Result.success1();
    }

    @GetMapping(value = "/search")
    public Result<Page<DeptResp>> findDepts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String parentName,
            @RequestParam(defaultValue = "1") int page, // 默认第1页
            @RequestParam(defaultValue = "10") int size // 默认10条
    ) {
        // 这里的 page 和 size 会传给 Service
        Page<DeptResp> result = deptService.findDepts(name, parentName, page, size);
        return Result.success2(result);
    }
}
