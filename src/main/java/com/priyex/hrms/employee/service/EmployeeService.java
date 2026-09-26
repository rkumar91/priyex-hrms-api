package com.priyex.hrms.employee.service;

import com.priyex.hrms.common.response.PagedResponse;
import com.priyex.hrms.employee.dto.CreateEmployeeRequest;
import com.priyex.hrms.employee.model.Employee;

public interface EmployeeService {

    PagedResponse<Employee> getEmployees(Long companyId, String query, Long departmentId, String status, int page, int size);

    Employee getEmployeeById(Long companyId, Long id);

    Employee createEmployee(Long companyId, Long actorUserId, CreateEmployeeRequest request);

    Employee updateEmployee(Long companyId, Long id, CreateEmployeeRequest request);

    void deleteEmployee(Long companyId, Long id);

    String exportEmployeesCsv(Long companyId);
}
