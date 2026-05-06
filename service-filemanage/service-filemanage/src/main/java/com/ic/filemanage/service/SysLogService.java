package com.ic.filemanage.service;

import com.ic.filemanage.common.PageResult;
import com.ic.filemanage.model.SysLogEntity;
import com.ic.filemanage.repository.SysLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SysLogService {
    @Autowired
    private SysLogRepository sysLogRepository;

    public PageResult<SysLogEntity> searchLogs(String operatorName, String type, LocalDateTime startTime, LocalDateTime endTime, int page, int size) {
        // 分页 + 排序 (按时间倒序)
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));

        Specification<SysLogEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. 按操作人姓名模糊查
            if (operatorName != null && !operatorName.isEmpty()) {
                predicates.add(cb.like(root.get("operatorName"), "%" + operatorName + "%"));
            }

            // 2. 按操作类型查
            if (type != null && !type.isEmpty()) {
                predicates.add(cb.equal(root.get("operationType"), type));
            }

            // 3. 按时间区间查
            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), startTime));
            }
            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createTime"), endTime));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<SysLogEntity> result = sysLogRepository.findAll(spec, pageable);
        return new PageResult<>(result.getTotalElements(), result.getContent());
    }
}
