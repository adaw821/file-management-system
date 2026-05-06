package com.ic.filemanage.modelDTO.req;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class UserSearchReq {
        private String keyword;

        private String departmentName;
        private Integer status;
        private Integer role;

        private int page = 1;
        private int size = 10;
    }

