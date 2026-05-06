package com.ic.filemanage.repository;

import com.ic.filemanage.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    @Query("SELECT f FROM FileEntity f WHERE " +
            "(:name IS NULL OR f.fileName LIKE %:name%) AND " +
            "(:type IS NULL OR f.type = :type) AND " +
            "(:createTime IS NULL OR f.createTime = :createTime)")
    List<FileEntity> searchFiles(String name, String type, LocalDateTime createTime);

    boolean existsByOwnerId(Long userId);
}
