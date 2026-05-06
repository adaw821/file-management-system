package com.ic.filemanage.common;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoLog {
    String value() default "";  // 操作类型，如 "新增用户"
    String module() default ""; // 模块，如 "系统管理"
}
