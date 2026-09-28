package com.priyex.hrms.employee.service.impl;

import com.priyex.hrms.common.exception.ResourceNotFoundException;
import com.priyex.hrms.employee.dto.CreateHrQueryRequest;
import com.priyex.hrms.employee.dto.HrPersonnelDto;
import com.priyex.hrms.employee.dto.RespondHrQueryRequest;
import com.priyex.hrms.employee.mapper.HrQueryMapper;
import com.priyex.hrms.employee.model.HrQuery;
import com.priyex.hrms.employee.model.HrQueryMessage;
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
    public HrQuery createQuery(Long companyId, Long employeeId, CreateHrQueryRequest req, String employeeName) {
        String initialStatus = (req.getAssignedHrId() != null) ? "ACTIVE" : "WAITING_IN_POOL";

        HrQuery record = HrQuery.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .assignedHrId(req.getAssignedHrId())
                .category(req.getCategory().toUpperCase())
                .subject(req.getSubject())
                .message(req.getMessage())
                .priority(req.getPriority() != null ? req.getPriority().toUpperCase() : "MEDIUM")
                .status(initialStatus)
                .build();

        hrQueryMapper.insert(record);

        // Add initial message into the conversation thread
        HrQueryMessage initialMsg = HrQueryMessage.builder()
                .queryId(record.getId())
                .senderUserId(null)
                .senderType("EMPLOYEE")
                .senderName(employeeName != null ? employeeName : "Employee")
                .messageText(req.getMessage())
                .build();
        hrQueryMapper.insertMessage(initialMsg);

        return hrQueryMapper.findById(record.getId(), companyId).orElse(record);
    }

    @Override
    public List<HrQuery> getQueries(Long companyId, Long employeeId, Long assignedHrId, String status) {
        return hrQueryMapper.findQueries(companyId, employeeId, assignedHrId, status);
    }

    @Override
    public List<HrQuery> getPoolQueries(Long companyId) {
        return hrQueryMapper.findPoolQueries(companyId);
    }

    @Override
    @Transactional
    public HrQuery claimQuery(Long companyId, Long queryId, Long hrUserId, String hrDisplayName) {
        HrQuery query = hrQueryMapper.findById(queryId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("HrQuery", "id", queryId));

        hrQueryMapper.claimQuery(queryId, companyId, hrUserId);

        // Post system welcome message from the HR specialist who claimed it
        HrQueryMessage welcomeMsg = HrQueryMessage.builder()
                .queryId(queryId)
                .senderUserId(hrUserId)
                .senderType("HR")
                .senderName(hrDisplayName != null ? hrDisplayName : "HR Specialist")
                .messageText("Hello! I have connected to your query. How can I help you today?")
                .build();
        hrQueryMapper.insertMessage(welcomeMsg);

        return hrQueryMapper.findById(queryId, companyId).orElse(query);
    }

    @Override
    @Transactional
    public HrQuery resolveQuery(Long companyId, Long queryId) {
        HrQuery query = hrQueryMapper.findById(queryId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("HrQuery", "id", queryId));

        hrQueryMapper.resolveQuery(queryId, companyId);
        return hrQueryMapper.findById(queryId, companyId).orElse(query);
    }

    @Override
    public HrQuery getActiveQueryForEmployee(Long companyId, Long employeeId) {
        return hrQueryMapper.findActiveQueryForEmployee(companyId, employeeId).orElse(null);
    }

    @Override
    @Transactional
    public HrQuery respondToQuery(Long companyId, Long queryId, RespondHrQueryRequest req) {
        HrQuery query = hrQueryMapper.findById(queryId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("HrQuery", "id", queryId));

        String newStatus = req.getStatus() != null ? req.getStatus().toUpperCase() : "RESOLVED";
        hrQueryMapper.respondToQuery(queryId, companyId, req.getResponse(), newStatus);

        // Also add response to message thread
        HrQueryMessage respMsg = HrQueryMessage.builder()
                .queryId(queryId)
                .senderUserId(query.getAssignedHrId())
                .senderType("HR")
                .senderName(query.getAssignedHrName() != null ? query.getAssignedHrName() : "HR Specialist")
                .messageText(req.getResponse())
                .build();
        hrQueryMapper.insertMessage(respMsg);

        return hrQueryMapper.findById(queryId, companyId).orElse(query);
    }

    @Override
    public List<HrPersonnelDto> getHrPersonnelList(Long companyId) {
        return hrQueryMapper.findHrPersonnelList(companyId);
    }

    @Override
    public List<HrQueryMessage> getMessages(Long companyId, Long queryId) {
        hrQueryMapper.findById(queryId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("HrQuery", "id", queryId));
        return hrQueryMapper.findMessagesByQueryId(queryId);
    }

    @Override
    @Transactional
    public HrQueryMessage sendMessage(Long companyId, Long queryId, Long senderUserId, String senderType, String senderName, String messageText) {
        hrQueryMapper.findById(queryId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("HrQuery", "id", queryId));

        HrQueryMessage msg = HrQueryMessage.builder()
                .queryId(queryId)
                .senderUserId(senderUserId)
                .senderType(senderType)
                .senderName(senderName)
                .messageText(messageText)
                .build();

        hrQueryMapper.insertMessage(msg);
        return msg;
    }
}
