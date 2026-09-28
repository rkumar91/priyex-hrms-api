package com.priyex.hrms.employee.service;

import com.priyex.hrms.employee.dto.CreateHrQueryRequest;
import com.priyex.hrms.employee.dto.HrPersonnelDto;
import com.priyex.hrms.employee.dto.RespondHrQueryRequest;
import com.priyex.hrms.employee.model.HrQuery;
import com.priyex.hrms.employee.model.HrQueryMessage;

import java.util.List;

public interface HrQueryService {

    HrQuery createQuery(Long companyId, Long employeeId, CreateHrQueryRequest request, String employeeName);

    List<HrQuery> getQueries(Long companyId, Long employeeId, Long assignedHrId, String status);

    List<HrQuery> getPoolQueries(Long companyId);

    HrQuery claimQuery(Long companyId, Long queryId, Long hrUserId, String hrDisplayName);

    HrQuery resolveQuery(Long companyId, Long queryId);

    HrQuery getActiveQueryForEmployee(Long companyId, Long employeeId);

    HrQuery respondToQuery(Long companyId, Long queryId, RespondHrQueryRequest request);

    List<HrPersonnelDto> getHrPersonnelList(Long companyId);

    // Message thread
    List<HrQueryMessage> getMessages(Long companyId, Long queryId);

    HrQueryMessage sendMessage(Long companyId, Long queryId, Long senderUserId, String senderType, String senderName, String messageText);
}
