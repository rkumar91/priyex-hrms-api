package com.priyex.hrms.employee.service;

import com.priyex.hrms.employee.dto.CreateHrQueryRequest;
import com.priyex.hrms.employee.dto.HrPersonnelDto;
import com.priyex.hrms.employee.dto.RespondHrQueryRequest;
import com.priyex.hrms.employee.model.HrQuery;

import java.util.List;

public interface HrQueryService {

    HrQuery createQuery(Long companyId, Long employeeId, CreateHrQueryRequest request);

    List<HrQuery> getQueries(Long companyId, Long employeeId, Long assignedHrId, String status);

    HrQuery respondToQuery(Long companyId, Long queryId, RespondHrQueryRequest request);

    List<HrPersonnelDto> getHrPersonnelList(Long companyId);
}
