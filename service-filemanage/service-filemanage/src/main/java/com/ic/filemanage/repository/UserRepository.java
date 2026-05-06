package com.ic.filemanage.repository;

import com.ic.filemanage.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    // UserRepository.java
    @Query("SELECT u FROM UserEntity u WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "u.username LIKE CONCAT('%', :keyword, '%') OR " +
            "u.name LIKE CONCAT('%', :keyword, '%')) " +
            "AND (:deptName IS NULL OR :deptName = '' OR u.departmentName = :deptName) " + // 加了空字符串判断
            "AND (:status IS NULL OR u.status = :status) " +
            "AND (:role IS NULL OR u.role = :role)")
    Page<UserEntity> search(
            @Param("keyword") String keyword,
            @Param("deptName") String deptName,
            @Param("status") Integer status,
            @Param("role") Integer role,
            Pageable pageable
    );

    List<UserEntity> findByDepartmentName(String deptName);

    boolean existsByUsername(String username);
}
