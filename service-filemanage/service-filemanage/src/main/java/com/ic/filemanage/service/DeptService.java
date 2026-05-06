package com.ic.filemanage.service;

import com.ic.filemanage.common.AutoLog;
import com.ic.filemanage.model.DepartmentEntity;
import com.ic.filemanage.model.UserEntity;
import com.ic.filemanage.modelDTO.req.DeptCreateReq;
import com.ic.filemanage.modelDTO.resp.DeptResp;
import com.ic.filemanage.repository.DeptRepository;
import com.ic.filemanage.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DeptService {
    @Autowired
    private DeptRepository deptRepository;
    @Autowired
    private UserRepository userRepository;

    @AutoLog(value = "新增部门", module = "部门管理")
    public void createDept(DeptCreateReq dept, UserEntity currentUser) {
        DepartmentEntity DEPT = new DepartmentEntity();

        DEPT.setDeptName(dept.getDeptName());
        DEPT.setLeaderId(currentUser.getUserId());

        DepartmentEntity parent = null;
        if (dept.getParentName() != null && !dept.getParentName().isEmpty()) {
            parent = deptRepository.findByDeptName(dept.getParentName());
            if (parent == null) {
                throw new RuntimeException("父部门不存在");
            }

            // 在设置 Parent 之后，保存之前，加上这段逻辑
            if (dept.getParentName() != null && !dept.getParentName().isEmpty()) {
                parent = deptRepository.findByDeptName(dept.getParentName());

                if (parent.getLevel() >= 3) {
                    throw new RuntimeException("部门层级不能超过 3 级");
                }
                DEPT.setLevel(parent.getLevel() + 1);

                DEPT.setParent(parent);
            } else {
                DEPT.setLevel(1);
            }

            // 子部门查重
            checkDuplicates(dept.getDeptName(), parent.getDeptId());
        } else {
            // 顶级部门查重
            checkDuplicates(dept.getDeptName(), null);
        }

        Integer orderNum = dept.getOrderNum();

        // 情况 A: 前端传了值，做个简单的负数校验
        if (orderNum != null) {
            if (orderNum < 0) {
                throw new RuntimeException("排序号不能为负数");
            }
            DEPT.setOrderNum(orderNum);
        }
        // 情况 B: 前端没传值，我们自动生成 (Max + 1)
        else {
            Integer maxOrder;
            if (parent == null) {
                // 查顶级部门的最大号
                maxOrder = deptRepository.findMaxOrderNumForTopLevel();
            } else {
                // 查这个父部门下的最大号
                maxOrder = deptRepository.findMaxOrderNumByParentId(parent.getDeptId());
            }
            // 设为最大号 + 1，排在最后面（如果是第一个部门，0+1=1，或者你可以改为默认0）
            DEPT.setOrderNum(maxOrder + 1);
        }

        deptRepository.save(DEPT);
    }

    @AutoLog(value = "编辑部门", module = "部门管理")
     public void updateDept(Long id, String newDeptName, Long newParentId, Integer orderNum) {
        // 查出旧数据
        DepartmentEntity DEPT = deptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("部门不存在"));

        // 检查是否把自己设为了父级
        if (newParentId != null && newParentId.equals(id)) {
            throw new RuntimeException("上级部门不能是自己");
        }

        // 检查改名逻辑
        boolean isNameChanged = newDeptName!= null && !newDeptName.equals(DEPT.getDeptName());

        // 检查修改父级逻辑
        boolean isParentChanged = false;
        if (newParentId != null) {
            Long oldParentId = (DEPT.getParent() != null) ? DEPT.getParent().getDeptId() : null;
            if (!newParentId.equals(oldParentId)) {
                isParentChanged = true;
            }
        }

        // 如果名字或父级变了，都要查重
        if (isNameChanged || isParentChanged) {
            String nameToCheck = isNameChanged ? newDeptName : DEPT.getDeptName();
            Long parentIdToCheck = isParentChanged ? newParentId : ((DEPT.getParent() != null) ? DEPT.getParent().getDeptId() : null);
            checkDuplicates(nameToCheck, parentIdToCheck);
        }

        List<UserEntity> users = new ArrayList<>();
        // 去掉 else if，让它们可以同时更新
        if (isNameChanged) {
            users = userRepository.findByDepartmentName(DEPT.getDeptName());
            DEPT.setDeptName(newDeptName);
        }

        if (isParentChanged) {
            DepartmentEntity newParent = new DepartmentEntity();
            newParent.setDeptId(newParentId);
            DEPT.setParent(newParent);
        }

        if (orderNum!= null && !orderNum.equals(DEPT.getOrderNum())) {
            DEPT.setOrderNum(orderNum);
        }

        //更新用户
        if(users != null && !users.isEmpty()){
            for(UserEntity user : users){
                user.setDepartmentName(DEPT.getDeptName());
            }
            userRepository.saveAll(users);
        }

        deptRepository.save(DEPT);
    }

    private void checkDuplicates(String nameToCheck, Long parentIdToCheck) {
        boolean exists;
        if (parentIdToCheck != null && parentIdToCheck > 0) {
            exists = deptRepository.existsByParent_DeptIdAndDeptName(parentIdToCheck, nameToCheck);
        } else {
            exists = deptRepository.existsByParentIsNullAndDeptName(nameToCheck);
        }

        if (exists) {
            throw new RuntimeException("同一上级部门下不可以重名");
        }
    }

    @AutoLog(value = "删除部门", module = "部门管理")
    public void deleteDept(Long id) {
        DepartmentEntity DEPT = deptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("不存在想要删除的部门"));

        if (DEPT.getUserNum() != 0) {
            throw new RuntimeException("用户数量不为零不可以删除部门");
        }

        boolean hasChildren = deptRepository.existsByParent_DeptId(id);
        if (hasChildren) {
            throw new RuntimeException("该部门下还有子部门，不能删除");
        }

        deptRepository.delete(DEPT);
    }

    // 参数类型改成了 String parentName
    public Page<DeptResp> findDepts(String name, String parentName, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        String searchName = (name != null && !name.isEmpty()) ? name : null;
        String searchParent = (parentName != null && !parentName.isEmpty()) ? parentName : null;

        Page<DepartmentEntity> entityPage = deptRepository.findByComplexSearch(searchName, searchParent, pageable);

        Page<DeptResp> respPage = entityPage.map(entity -> {
            DeptResp deptResp = new DeptResp();
            BeanUtils.copyProperties(entity, deptResp);

            if (entity.getParent() != null) {
                deptResp.setParentName(entity.getParent().getDeptName());
            } else {
                deptResp.setParentName("最高级");
            }

            return deptResp;
        });

        return respPage;
    }
}
