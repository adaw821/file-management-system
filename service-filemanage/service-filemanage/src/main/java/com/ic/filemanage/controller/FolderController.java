package com.ic.filemanage.controller;
import com.ic.filemanage.common.Result;
import com.ic.filemanage.model.UserEntity;
import com.ic.filemanage.modelDTO.req.FolderCreateReq;
import com.ic.filemanage.modelDTO.resp.FolderResp;
import com.ic.filemanage.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/api/folder")

public class FolderController {
    @Autowired

    private FolderService folderService;
    @PostMapping(value = "/create")
    public Result<Void> CreateFolder(@RequestBody FolderCreateReq req, @RequestAttribute("loginUser")UserEntity currentUser){
        folderService.CreateFolder(req, currentUser);
        return Result.success1();
    }


    @PutMapping(value = "/rename/{id}")
    public  Result<Void> RenameFolder(@PathVariable Long id, @RequestParam String newName, @RequestAttribute("loginUser")UserEntity currentUser){
        folderService.RenameFolder(id, newName, currentUser);
        return Result.success1();
    }

    @DeleteMapping(value = "/delete/{id}")
    public Result<Void> DeleteFolder(@PathVariable Long id, @RequestAttribute("loginUser")UserEntity currentUser){
        folderService.DeleteFolder(id, currentUser);
        return Result.success1();
    }

    @PutMapping(value = "/moveFolder/{id}")
    public Result<Void> MoveFolder(@PathVariable Long id, @RequestParam Long parentId, @RequestAttribute("loginUser")UserEntity currentUser){
        folderService.MoveFolder(id, parentId, currentUser);
        return Result.success1();
    }

    @GetMapping(value = "/listFolders/{id}")
    public Result<FolderResp> GetFolderList (@PathVariable Long parentId, @RequestAttribute("loginUser")UserEntity currentUser){
        folderService.GetFolderList(parentId, currentUser);
        return Result.success1();
    }


    @PutMapping(value = "/permission_set/{id}")
    public Result<Void> PermissionSetting (@PathVariable Long id, @RequestParam Integer permission, @RequestAttribute("loginUser")UserEntity currentUser){
        folderService.PermissionSetting(id, permission, currentUser);
        return Result.success1();
    }
}
