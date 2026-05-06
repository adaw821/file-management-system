package com.ic.filemanage.repository;

import com.ic.filemanage.model.LogEntity;
import com.ic.filemanage.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByUsername(String username);
}
