package com.ic.filemanage.repository;

import com.ic.filemanage.model.FolderEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<FolderEntity, Long> {

    // 查父ID下的列表
    List<FolderEntity> findByParentId(Long parentId);

    // 查重 (用ID查，不要用Name查)
    boolean existsByParentIdAndFoldName(Long parentId, String foldName);

    // 查是否有子文件夹
    boolean existsByParentId(Long parentId);

    FolderEntity findByFoldName(String parentName);
}
