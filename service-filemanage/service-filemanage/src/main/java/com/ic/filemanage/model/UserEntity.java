package com.ic.filemanage.model;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sys_user")
@Data  // Lombok自动生成getter/setter
@NoArgsConstructor  // 无参构造器
@AllArgsConstructor // 全参构造器

public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name", nullable = false, unique = false)
    private String name;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;  // 字段名应该叫username，不是name

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;  // 手机号用String

    @Column(name = "department_name", length = 50)
    private String departmentName;

    @Column(name = "status", nullable = false)
    private Integer status;//1在职 0离职

    @Column(name = "role", nullable = false) //1管理员 2普通用户
    private Integer role;

    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();
}