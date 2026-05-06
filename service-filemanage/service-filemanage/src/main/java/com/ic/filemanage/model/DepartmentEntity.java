package com.ic.filemanage.model;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "department")
@Data  // Lombok自动生成getter/setter
@NoArgsConstructor  // 无参构造器
@AllArgsConstructor // 全参构造器

public class DepartmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dept_id")
    private Long deptId;

    // 部门名称
    @Column(name = "dept_name", nullable = false, length = 30)
    private String deptName;

    // 上级部门
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private DepartmentEntity parent;

    // 排序号：非负整数，用于展示顺序
    @Column(name = "order_num", nullable = false)
    @Min(value = 0, message = "排序号不能为负数")
    private Integer orderNum = 0;

    // 部门层级（根部门为1，子部门递增）
    @Column(name = "dept_level", nullable = false)
    private Integer Level = 1;

    // 部门负责人ID
    @Column(name = "leader_id")
    private Long leaderId;

    // 创建时间
    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(name = "user_number")
    private int userNum;



    /**
     * 判断是否为根部门
     */
    public boolean isRoot() {
        return parent == null;
    }

}