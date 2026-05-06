package com.ic.filemanage.service;

import com.ic.filemanage.common.AutoLog;
import com.ic.filemanage.model.DepartmentEntity;
import com.ic.filemanage.model.FileEntity;
import com.ic.filemanage.model.FolderEntity;
import com.ic.filemanage.model.UserEntity;
import com.ic.filemanage.modelDTO.req.FolderCreateReq;
import com.ic.filemanage.modelDTO.resp.FolderResp;
import com.ic.filemanage.repository.DeptRepository;
import com.ic.filemanage.repository.FolderRepository;
import com.ic.filemanage.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FolderService {
    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private UserRepository userRepository;

    @AutoLog(value = "文件夹上传", module = "文件管理")
    public void CreateFolder(FolderCreateReq req, UserEntity currentUser) {
        if (currentUser == null) {
            throw new RuntimeException("登录已失效");
        }

        // 1. 先确定父文件夹 ID
        Long parentId = 0L; // 默认为根目录
        FolderEntity parentFolder = null;

        // 如果前端传了父文件夹名字，就去查它的 ID
        if (req.getParentName() != null && !req.getParentName().isEmpty()) {
            parentFolder = folderRepository.findByFoldName(req.getParentName());
            if (parentFolder == null) {
                // 如果按名字没查到，说明父文件夹不存在，报错
                throw new RuntimeException("父文件夹不存在：" + req.getParentName());
            }
            parentId = parentFolder.getFoldId();
        }

        // 2. ✅ 使用 ID 进行查重 (修复报错的关键)
        // 翻译：在 parentId 这个父级下，有没有叫 foldName 的文件夹？
        if (folderRepository.existsByParentIdAndFoldName(parentId, req.getFoldName())) {
            throw new RuntimeException("该目录下已存在同名文件夹");
        }

        // 3. 组装实体并保存
        FolderEntity folder = new FolderEntity();
        folder.setFoldName(req.getFoldName());
        folder.setAuthType(req.getAuthType());
        folder.setCreatorId(currentUser.getUserId());
        folder.setCreateTime(LocalDateTime.now());

        // 设置父级信息
        folder.setParentId(parentId);
        if (parentFolder != null) {
            // 是子文件夹
            if (parentFolder.getLevel() + 1 > 5) {
                throw new RuntimeException("层级不能超过 5 层");
            }
            folder.setLevel(parentFolder.getLevel() + 1);
        } else {
            // 是根目录
            folder.setLevel(1);
        }

        folderRepository.save(folder);
    }

    @AutoLog(value = "文件夹重命名", module = "文件管理")
    public void RenameFolder(Long id, String newName, UserEntity currentUser) {
        FolderEntity folder = folderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文件夹不存在"));

        if (!canOperate(currentUser, folder.getCreatorId())) {
            throw new RuntimeException("没有操作权限");
        }

        if (folderRepository.existsByParentIdAndFoldName(folder.getParentId(), newName)) {
            throw new RuntimeException("重名了");
        }

        folder.setFoldName(newName);
        folderRepository.save(folder);
    }

    @AutoLog(value = "删除文件夹", module = "文件管理")
    public void DeleteFolder(Long id, UserEntity currentUser) {
        FolderEntity folder = folderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文件夹不存在"));

        if (!canOperate(currentUser, folder.getCreatorId())) {
            throw new RuntimeException("没有操作权限");
        }

        List<FileEntity> files = folder.getFiles();
        if (files != null && !files.isEmpty()) {
            throw new RuntimeException("文件夹中有文件，不可删除");
        }

        if (folderRepository.existsByParentId(id)) {
            throw new RuntimeException("该文件夹下还有子文件夹，不可删除");
        }

        folderRepository.delete(folder);
    }

    @AutoLog(value = "移动文件夹", module = "文件管理")
    public void MoveFolder(Long id, Long targetParentId, UserEntity currentUser) {
        FolderEntity folder = folderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文件夹不存在"));

        if (!canOperate(currentUser, folder.getCreatorId())) {
            throw new RuntimeException("没有操作权限");
        }

        if (folder.getFoldId().equals(targetParentId)) {
            throw new RuntimeException("不能移动到自身");
        }

        int newLevel = 1;
        if (targetParentId != 0) {
            FolderEntity targetParent = folderRepository.findById(targetParentId)
                    .orElseThrow(() -> new RuntimeException("目标文件夹不存在"));
            newLevel = targetParent.getLevel() + 1;
            if (newLevel > 5) throw new RuntimeException("移动后层级超过限制");
        }

        folder.setParentId(targetParentId);
        folder.setLevel(newLevel);
        folderRepository.save(folder);
    }

    public List<FolderResp> GetFolderList(Long parentId, UserEntity user) {
        if (user == null) throw new RuntimeException("用户不存在");

        Long pid = (parentId == null) ? 0L : parentId;

        List<FolderEntity> list = folderRepository.findByParentId(pid);

        List<FolderResp> resps = convertToResp(list, user);

        //按创建时间降序
        return resps.stream()
                .sorted(Comparator.comparing(FolderResp::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @AutoLog(value = "修改文件夹权限", module = "文件管理")
    public void PermissionSetting(Long id, Integer permission, UserEntity currentUser) {
        FolderEntity folder = folderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文件夹不存在"));

        if (!canOperate(currentUser, folder.getCreatorId())) {
            throw new RuntimeException("没有权限修改设置");
        }

        if (permission == null || permission < 0 || permission > 2) {
            throw new RuntimeException("无效的权限类型");
        }

        folder.setAuthType(permission);
        folderRepository.save(folder);
    }

    public List<FolderResp> convertToResp(List< FolderEntity> folderEntities, UserEntity user){
        List<FolderResp> folderResps = new ArrayList<>();

        if(folderEntities == null || folderEntities.isEmpty()){
            throw new RuntimeException("没查到东西");
        }

        for(FolderEntity entity : folderEntities){
            FolderResp resp = new FolderResp();
            Boolean check = false;
            switch (entity.getAuthType()){
                case 0:
                    if(entity.getCreatorId() == user.getUserId()){
                        check = true;
                    }
                    break;

                case 1:
                    UserEntity creator = userRepository.findById(entity.getCreatorId()).get();
                    if (creator != null && Objects.equals(creator.getDepartmentName(), user.getDepartmentName())) {
                        check = true;
                    }
                    break;

                case 2:
                    check = true;
                    break;

                default:
                    throw new RuntimeException("权限不对");


            }

            if(check){
                BeanUtils.copyProperties(entity, resp);
                folderResps.add(resp);
            }
        }

        return folderResps;
    }

    public boolean canOperate(UserEntity user, Long creator_id){
        if(user.getRole() == 1){
            return true;
        }

        if(user.getUserId() == creator_id){
            return true;
        }

        return false;
    }

}
