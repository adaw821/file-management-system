package com.ic.filemanage.service;

import com.ic.filemanage.common.AutoLog;
import com.ic.filemanage.common.PageResult;
import com.ic.filemanage.model.DepartmentEntity;
import com.ic.filemanage.model.UserEntity;
import com.ic.filemanage.modelDTO.req.UserCreateReq;
import com.ic.filemanage.modelDTO.req.UserEditReq;
import com.ic.filemanage.modelDTO.req.UserSearchReq;
import com.ic.filemanage.modelDTO.resp.UserResp;
import com.ic.filemanage.repository.DeptRepository;
import com.ic.filemanage.repository.FileRepository;
import com.ic.filemanage.repository.UserRepository;
import com.ic.filemanage.utils.Md5Utils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DeptRepository deptRepository;
    @Autowired
    private FileRepository fileRepository;

    @AutoLog(value = "新增用户", module = "用户管理")
    public void createUser(UserCreateReq req) {
        UserEntity user = new UserEntity();

        if (req.getUsername() == null || !Pattern.matches("^[a-zA-Z0-9]{6,20}$", req.getUsername())) {
            throw new RuntimeException("用户名格式错误：必须是 6-20 位的字母或数字组合");
        }

        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("用户名 [" + req.getUsername() + "] 已被占用，请换一个");
        }

        user.setUsername(req.getUsername());
        user.setName(req.getName());
        user.setPhoneNumber(req.getPhoneNumber()); // 注意：Req里是 phoneNumber，Entity里也是 phoneNumber
        user.setDepartmentName(req.getDepartmentName());
        user.setRole(req.getRole());
        user.setStatus(req.getStatus());

        // 设置默认密码
        user.setPassword(Md5Utils.code("123456"));

        String deptName = user.getDepartmentName();
        if (deptName != null && !deptName.isEmpty()) {
            DepartmentEntity dept = deptRepository.findByDeptName(deptName);
            if (dept == null) {
                throw new RuntimeException("部门 [" + deptName + "] 不存在！");
            }
            // 防止空指针的写法
            int currentNum = dept.getUserNum();
            if (user.getStatus() != 0) {
                dept.setUserNum(currentNum + 1);
                deptRepository.save(dept);
            }
        }

        userRepository.save(user);
    }

    @AutoLog(value = "编辑用户", module = "用户管理")
    public void editUser(UserEditReq req, String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        String oldDeptName = user.getDepartmentName();
        String newDeptName = req.getDepartmentName();

        if (req.getName() != null && !req.getName().isEmpty()) {
            user.setName(req.getName());
        }

        if (req.getPhoneNumber() != null && !req.getPhoneNumber().isEmpty()) {
            user.setPhoneNumber(req.getPhoneNumber());
        }

        if (req.getDepartmentName() != null && !req.getDepartmentName().isEmpty()) {
            if(deptRepository.findByDeptName(req.getDepartmentName()) == null){
                throw new RuntimeException("需要输入已有的部门");
            }
            user.setDepartmentName(req.getDepartmentName());
        }

        if (req.getRole() != null) {
            user.setRole(req.getRole());
        }

        if (req.getStatus() != null) {
            user.setStatus(req.getStatus());
        }

        boolean isDeptChanged = !Objects.equals(oldDeptName, newDeptName);

        if (isDeptChanged) {
            if (oldDeptName != null) {
                DepartmentEntity oldDept = deptRepository.findByDeptName(oldDeptName);
                if (oldDept != null && oldDept.getUserNum() > 0) {
                    oldDept.setUserNum(oldDept.getUserNum() - 1);
                    deptRepository.save(oldDept);
                }
            }

            if (newDeptName != null) {
                DepartmentEntity newDept = deptRepository.findByDeptName(newDeptName);
                if (newDept != null) {
                    int currentNum = newDept.getUserNum();
                    newDept.setUserNum(currentNum + 1);
                    deptRepository.save(newDept);
                }
            }
        }
        userRepository.save(user);
    }

    public PageResult<UserResp> searchUsers(UserSearchReq req) {
        // 防止 page 传 0 或负数报错
        int page = req.getPage() < 1 ? 0 : req.getPage() - 1;
        Pageable pageable = PageRequest.of(page, req.getSize());

        Page<UserEntity> pageResult = userRepository.search(
                req.getKeyword(),
                req.getDepartmentName(),
                req.getStatus(),
                req.getRole(),
                pageable
        );

        List<UserResp> respList = new ArrayList<>();
        if (pageResult != null && pageResult.hasContent()) {
            for (UserEntity user : pageResult.getContent()) {
                UserResp resp = new UserResp();
                BeanUtils.copyProperties(user, resp);
                respList.add(resp);
            }
            return new PageResult<>(pageResult.getTotalElements(), respList);
        }

        return new PageResult<>(0L, new ArrayList<>());
    }

    @AutoLog(value = "重置密码", module = "用户管理")
    public void resetPassword(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setPassword(Md5Utils.code("123456"));
        userRepository.save(user);
    }

    @AutoLog(value = "删除用户", module = "用户管理")
    public void deleteUser(Long userId, UserEntity currentUser) {
        // 1. 权限校验
        if (currentUser.getRole() != 1) {
            throw new RuntimeException("无权操作：只有管理员可以删除用户");
        }

        // 2. 查出要删的人
        UserEntity userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("该用户不存在"));

        // 3. 安全校验：不能删除自己 (防止管理员误删自己导致后台进不去)
        if (userToDelete.getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("无法删除当前登录账号");
        }

        // 4. 数据完整性校验：该用户是否还有未删除的文件？
        // 需要去 FileRepository 加一个方法: boolean existsByOwnerId(Long ownerId);
        if (fileRepository.existsByOwnerId(userId)) {
            throw new RuntimeException("该用户名下还有文件，请先转移或删除文件后再试");
        }


        // 6. 维护部门人数 (如果他在职，删除前要把部门人数 -1)
        if (userToDelete.getStatus() == 1 && userToDelete.getDepartmentName() != null) {
            DepartmentEntity dept = deptRepository.findByDeptName(userToDelete.getDepartmentName());
            if (dept != null && dept.getUserNum() > 0) {
                dept.setUserNum(dept.getUserNum() - 1);
                deptRepository.save(dept);
            }
        }

        // 7. 执行物理删除
        userRepository.delete(userToDelete);
    }

    @AutoLog(value = "用户离职", module = "用户管理")
    public void setResign(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (user.getStatus() != null && user.getStatus() == 0) {
            return;
        }

        String deptName = user.getDepartmentName();
        if (deptName != null) {
            DepartmentEntity dept = deptRepository.findByDeptName(deptName);
            if (dept != null && dept.getUserNum() > 0) {
                dept.setUserNum(dept.getUserNum() - 1);
                deptRepository.save(dept);
            }
        }

        user.setStatus(0); // 0 代表离职
        userRepository.save(user);
    }
}