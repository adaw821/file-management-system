package com.ic.filemanage.service;

import com.ic.filemanage.common.AutoLog;
import com.ic.filemanage.model.FileEntity;
import com.ic.filemanage.model.FolderEntity;
import com.ic.filemanage.model.UserEntity;
import com.ic.filemanage.modelDTO.resp.FileResp;
import com.ic.filemanage.repository.FileRepository;
import com.ic.filemanage.repository.FolderRepository;
import com.ic.filemanage.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private UserRepository userRepository;
  // 建议在 application.yml 配置: file.upload-dir=D:/upload_files/
    @Value("${file.upload-dir:C:/file_storage/}")
    private String baseUploadPath;

    @AutoLog(value = "文件上传", module = "文件管理")
    public void createFile(Long parentId, String name, MultipartFile file, UserEntity currentUser) {
        // --- 基础校验 ---
        if (file == null || file.isEmpty()) throw new RuntimeException("没有上传文件");
        if (currentUser == null) throw new RuntimeException("登录已失效");

        // --- 确定父文件夹 & 继承权限 ---
        Long finalParentId = (parentId == null) ? 0L : parentId;
        Integer authType = 0; // 默认私有
        if (finalParentId != 0) {
            FolderEntity folder = folderRepository.findById(finalParentId)
                    .orElseThrow(() -> new RuntimeException("上传的目标文件夹不存在"));
            authType = folder.getAuthType();
        }

        // --- 格式校验 ---
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new RuntimeException("文件缺少后缀");
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String extensionCheck = suffix.substring(1).toLowerCase();

        List<String> allowedExtensions = Arrays.asList("jpg", "png", "doc", "docx", "pdf", "xlsx", "ppt", "pptx");
        if (!allowedExtensions.contains(extensionCheck)) {
            throw new RuntimeException("不支持的文件格式: " + extensionCheck);
        }

        if (file.getSize() > 100 * 1024 * 1024) {
            throw new RuntimeException("单文件不能超过 100MB");
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        // 最终目录: C:/file_storage/2026/02/01/
        File dir = new File(baseUploadPath + File.separator + datePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String uuid = UUID.randomUUID().toString().replace("-", "");
        String newFileName = uuid + suffix; // 磁盘上的文件名
        File dest = new File(dir, newFileName); // 创建文件对象

        try {
            file.transferTo(dest); // 写入磁盘
        } catch (IOException e) {
            throw new RuntimeException("文件写入磁盘失败", e);
        }

        FileEntity newFile = new FileEntity();
        newFile.setFileName(name); // 显示名 (原文件名)
        newFile.setFileName(originalFilename); // 原始文件名
        newFile.setUuid_name(newFileName); // 唯一文件名
        newFile.setFileSize(file.getSize());
        newFile.setType(extensionCheck); // 记录类型 doc/png...

        newFile.setOwnerId(currentUser.getUserId());
        newFile.setParentId(finalParentId);
        newFile.setPermissionType(authType); // 写入继承的权限
        newFile.setFile_path(dest.getAbsolutePath()); // 绝对路径
        newFile.setCreateTime(LocalDateTime.now());
        newFile.setTimes(0); // 初始化下载次数

        fileRepository.save(newFile);
    }

    @AutoLog(value = "文件重命名", module = "文件管理")
    public void renameFile(Long id, String name, UserEntity currentUser) {
        FileEntity file = fileRepository.findById(id).orElseThrow(() -> new RuntimeException("文件不存在"));

        if (!canOperate(currentUser, file.getOwnerId())) {
            throw new RuntimeException("没有修改权限");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("文件名不能为空");
        }
        if (name.length() > 100) { // 需求：长度限制
            throw new RuntimeException("文件名过长");
        }

        file.setFileName(name);
        fileRepository.save(file);
    }


    @AutoLog(value = "文件删除", module = "文件管理")
    public void deleteFile(Long id, UserEntity currentUser) {
        FileEntity file = fileRepository.findById(id).orElseThrow(() -> new RuntimeException("文件不存在"));

        if (!canOperate(currentUser, file.getOwnerId())) {
            throw new RuntimeException("没有删除权限");
        }

        // 1. 删除本地物理文件
        if (file.getFile_path() != null) {
            File diskFile = new File(file.getFile_path());
            if (diskFile.exists()) {
                boolean deleted = diskFile.delete();
                if (!deleted) {
                    // 实际生产中这里可以记录日志，但不一定要阻断流程
                    System.err.println("警告：物理文件删除失败 " + file.getFile_path());
                }
            }
        }

        // 2. 删除数据库记录
        fileRepository.delete(file);
    }

    @AutoLog(value = "文件移动", module = "文件管理")
    public void moveFile(Long id, Long targetParentId, UserEntity currentUser) {
        FileEntity file = fileRepository.findById(id).orElseThrow(() -> new RuntimeException("文件不存在"));

        if (!canOperate(currentUser, file.getOwnerId())) {
            throw new RuntimeException("没有移动权限");
        }

        // 移动到根目录
        if (targetParentId == null || targetParentId == 0) {
            file.setParentId(0L);
            file.setPermissionType(0); // 根目录默认私有
            fileRepository.save(file);
            return;
        }

        FolderEntity targetFolder = folderRepository.findById(targetParentId)
                .orElseThrow(() -> new RuntimeException("目标文件夹不存在"));

        // 更新位置和权限
        file.setParentId(targetParentId);
        file.setPermissionType(targetFolder.getAuthType()); // 需求：权限随新文件夹继承

        fileRepository.save(file);
    }

    @AutoLog(value = "文件下载", module = "文件管理")
    public FileEntity downloadFile(Long id, UserEntity currentUser) {
        FileEntity file = fileRepository.findById(id).orElseThrow(() -> new RuntimeException("文件不存在"));

        if (!checkPermission(file, currentUser)) {
            throw new RuntimeException("无权下载此文件");
        }

        // 更新下载次数
        file.setTimes(file.getTimes() == null ? 1 : file.getTimes() + 1);
        fileRepository.save(file);

        return file;
    }


    public List<FileResp> searchFile(String name, String type, LocalDateTime startTime, LocalDateTime endTime, UserEntity user) {
        if (user == null) return new ArrayList<>();

        // 这里的 searchFiles SQL 需要你根据 type/time 修改一下支持区间查询
        List<FileEntity> files = fileRepository.searchFiles(name, type, startTime);

        List<FileResp> result = new ArrayList<>();
        if (files == null || files.isEmpty()) return result;

        for (FileEntity file : files) {
            // 复用鉴权逻辑
            if (checkPermission(file, user)) {
                FileResp fileResp = new FileResp();
                BeanUtils.copyProperties(file, fileResp);
                UserEntity creator = userRepository.findById(file.getOwnerId()).get();
                fileResp.setOwnerName(creator.getName());
                fileResp.setFileName(file.getFileName());
                result.add(fileResp);
            }
        }
        return result;
    }

    private boolean checkPermission(FileEntity file, UserEntity user) {
        // 1. 管理员
        if (user.getRole() == 1) return true;
        // 2. 自己的文件
        if (file.getOwnerId().equals(user.getUserId())) return true;

        // 3. 根据 PermissionType 判断
        Integer type = file.getPermissionType();
        if (type == null) type = 0; // 默认为私有

        if (type == 2) { // 全员
            return true;
        }
        else if (type == 1) { // 部门
            UserEntity owner = userRepository.findById(file.getOwnerId()).orElse(null);
            if (owner != null && Objects.equals(owner.getDepartmentName(), user.getDepartmentName())) {
                return true;
            }
        }
        return false;
    }

    private boolean canOperate(UserEntity user, Long ownerId) {
        return user.getRole() == 1 || user.getUserId().equals(ownerId);
    }
}