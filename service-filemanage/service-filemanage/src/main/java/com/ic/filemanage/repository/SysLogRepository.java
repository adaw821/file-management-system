package com.ic.filemanage.repository;

import com.ic.filemanage.model.SysLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SysLogRepository extends JpaRepository<SysLogEntity, Long>, JpaSpecificationExecutor<SysLogEntity> {
}
