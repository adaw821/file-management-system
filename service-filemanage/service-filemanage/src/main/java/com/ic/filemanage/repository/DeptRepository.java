package com.ic.filemanage.repository;

import com.ic.filemanage.model.DepartmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DeptRepository extends JpaRepository<DepartmentEntity, Long> {

    boolean existsByParent_DeptIdAndDeptName(Long parentId, String deptName);

    boolean existsByParentIsNullAndDeptName(String deptName);

    // 新增：检查某个 ID 是否是别人的父级 (用于删除检查)
    boolean existsByParent_DeptId(Long parentId);

    @Query("SELECT f FROM DepartmentEntity f WHERE (:name IS NULL OR f.deptName LIKE %:name%) AND (:parentId IS NULL OR f.parent.deptId = :parentId)")
    Page<DepartmentEntity> findDepts(String name, Long parentId, Pageable pageable);


    @Query("SELECT d FROM DepartmentEntity d " +
            "LEFT JOIN d.parent p " +  // 左连接，防止没有父级的部门查不出来
            "WHERE (:name IS NULL OR d.deptName LIKE %:name%) " +
            "AND (:parentName IS NULL OR p.deptName LIKE %:parentName%)")
    Page<DepartmentEntity> findByComplexSearch(String name, String parentName, Pageable pageable);

    DepartmentEntity findByDeptName(String deptName);

    @Query("select coalesce(max(d.orderNum), 0) from DepartmentEntity d where d.parent is null")
    Integer findMaxOrderNumForTopLevel();

    @Query("select coalesce(max(d.orderNum), 0) from DepartmentEntity d where d.parent.deptId = :parentId")
    Integer findMaxOrderNumByParentId(Long parentId);
}
