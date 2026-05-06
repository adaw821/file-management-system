package com.ic.filemanage.model;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ic.filemanage.common.AuthType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "Folder")
@Data  // Lombok自动生成getter/setter
@NoArgsConstructor  // 无参构造器
@AllArgsConstructor // 全参构造器

public class FolderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fold_id")
    private Long foldId;

    @Column(name = "fold_name", length = 50, nullable = false)
    private String foldName;

    @Column(name = "parent_id", nullable = false)
    private Long parentId;

    @Column(name = "permission", nullable = false)
    private Integer authType;//0私有， 1部门可见， 2全员可见

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(name = "createTime", nullable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    @OneToMany
    @JoinColumn(name = "folder_id")
    private List<FileEntity> files = new ArrayList<>();

    @Column(name = "level", length = 5)
    private int level;
}