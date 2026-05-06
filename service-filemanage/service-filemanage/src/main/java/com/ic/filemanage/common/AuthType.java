package com.ic.filemanage.common;

public class AuthType {
    //权限设置	三级权限：1. 私有：仅创建人可见；2. 部门可见：创建人所在部门所有 “在职” 用户可见；3. 全员可见：系统所有 “在职” 用户可见
    private Integer individual = 0;
    private Integer dept = 1;
    private Integer all = 2;
}
