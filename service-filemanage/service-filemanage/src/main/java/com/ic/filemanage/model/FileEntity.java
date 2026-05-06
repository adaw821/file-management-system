package com.ic.filemanage.model;
import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "FileInfo")
@Data  // Lombok自动生成getter/setter
@NoArgsConstructor  // 无参构造器
@AllArgsConstructor // 全参构造器

public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "file_name", nullable = false, length = 100)
    private String fileName;

    @Column(name = "uuid_name", nullable = false)
    private String uuid_name;

    @Column(name = "parent_id", nullable = false)
    private Long parentId = 0L;  // 0表示根目录

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;  // 创建人ID


    @Column(name = "file_type", nullable = false)
    private String type;


    @Column(name = "permission_type", nullable = false)
    private Integer permissionType;  // 0-私有, 1-部门可见, 2-全员可见

    @Column(name = "status", nullable = false)
    private Integer status = 1;  // 1-正常, 0-删除

    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(name = "file_size")
    private Long FileSize;

    @Column(name = "download_times")
    private Integer times;

    @Column(name = "file_path")
    private String file_path;

}