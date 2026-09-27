package com.priyex.hrms.employee.service;

import com.priyex.hrms.common.response.PagedResponse;
import com.priyex.hrms.employee.dto.CreateEmployeeRequest;
import com.priyex.hrms.employee.model.Employee;

public interface EmployeeService {

    PagedResponse<Employee> getEmployees(Long companyId, String query, Long departmentId, String status, int page, int size);

    Employee getEmployeeById(Long companyId, Long id);

    Employee getMyProfile(Long companyId, Long userId, Long employeeId);

    Employee updateMyProfile(Long companyId, Long userId, Long employeeId, com.priyex.hrms.employee.dto.UpdateSelfProfileRequest request);

    Employee createEmployee(Long companyId, Long actorUserId, CreateEmployeeRequest request);

    Employee updateEmployee(Long companyId, Long id, CreateEmployeeRequest request);

    void deleteEmployee(Long companyId, Long id);

    String exportEmployeesCsv(Long companyId);

    java.util.List<com.priyex.hrms.employee.model.EmployeeDocument> getDocuments(Long employeeId);

    com.priyex.hrms.employee.model.EmployeeDocument uploadDocument(Long employeeId, Long actorUserId, com.priyex.hrms.employee.dto.UploadDocumentRequest req);

    void deleteDocument(Long employeeId, Long documentId);
}
