package com.ic.filemanage.controller;

import com.ic.filemanage.common.Result;
import com.ic.filemanage.model.UserEntity;
import com.ic.filemanage.modelDTO.resp.FileResp;
import com.ic.filemanage.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/file")
public class FileController {
    @Autowired
    private FileService fileService;

    @PostMapping("/create_file")
    public Result<Void> CreateFile(@RequestParam Long parentId, @RequestParam String name, MultipartFile file, @RequestAttribute ("loginUser")UserEntity currentUser){

        fileService.createFile(parentId, name, file, currentUser);
        return Result.success1();
    }

    @GetMapping("/search_file")
    public Result<List<FileResp>> searchFile(@RequestParam String name, @RequestParam String type, @RequestParam LocalDateTime startTime, @RequestParam LocalDateTime endTime, @RequestAttribute ("loginUser")UserEntity currentUser ){
        List<FileResp> resp = fileService.searchFile(name, type, startTime, endTime, currentUser);
        return Result.success2(resp);
    }


    @PutMapping("/rename_file/{id}")
    public Result<Void> renameFile(@PathVariable Long id, @RequestParam String name, @RequestAttribute ("loginUser")UserEntity currentUser){
        fileService.renameFile(id, name, currentUser);
        return Result.success1();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteFile(@PathVariable Long id, @RequestParam Long userId,@RequestAttribute ("loginUser")UserEntity currentUser){
        fileService.deleteFile(id, currentUser);
        return Result.success1();
    }

    @PutMapping("/move/{id}")
    public Result<Void> moveFile(@PathVariable Long id, @RequestParam Long parentId, @RequestAttribute ("loginUser")UserEntity currentUser){
        fileService.moveFile(id, parentId, currentUser);
        return Result.success1();
    }



}
