package com.priyex.hrms.employee.service.impl;

import com.priyex.hrms.common.exception.ResourceNotFoundException;
import com.priyex.hrms.employee.dto.CreateHrQueryRequest;
import com.priyex.hrms.employee.dto.HrPersonnelDto;
import com.priyex.hrms.employee.dto.RespondHrQueryRequest;
import com.priyex.hrms.employee.mapper.HrQueryMapper;
import com.priyex.hrms.employee.model.HrQuery;
import com.priyex.hrms.employee.service.HrQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HrQueryServiceImpl implements HrQueryService {

    private final HrQueryMapper hrQueryMapper;

    @Override
    @Transactional
    public HrQuery createQuery(Long companyId, Long employeeId, CreateHrQueryRequest req) {
        HrQuery record = HrQuery.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .assignedHrId(req.getAssignedHrId())
                .category(req.getCategory().toUpperCase())
                .subject(req.getSubject())
                .message(req.getMessage())
                .priority(req.getPriority() != null ? req.getPriority().toUpperCase() : "MEDIUM")
                .status("OPEN")
                .build();

        hrQueryMapper.insert(record);
        return hrQueryMapper.findById(record.getId(), companyId).orElse(record);
    }

    @Override
    public List<HrQuery> getQueries(Long companyId, Long employeeId, Long assignedHrId, String status) {
        return hrQueryMapper.findQueries(companyId, employeeId, assignedHrId, status);
    }

    @Override
    @Transactional
    public HrQuery respondToQuery(Long companyId, Long queryId, RespondHrQueryRequest req) {
        HrQuery query = hrQueryMapper.findById(queryId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("HrQuery", "id", queryId));

        String newStatus = req.getStatus() != null ? req.getStatus().toUpperCase() : "RESOLVED";
        hrQueryMapper.respondToQuery(queryId, companyId, req.getResponse(), newStatus);
        return hrQueryMapper.findById(queryId, companyId).orElse(query);
    }

    @Override
    public List<HrPersonnelDto> getHrPersonnelList(Long companyId) {
        return hrQueryMapper.findHrPersonnelList(companyId);
    }
}
