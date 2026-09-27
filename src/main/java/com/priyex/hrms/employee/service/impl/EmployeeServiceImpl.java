package com.priyex.hrms.employee.service.impl;

import com.priyex.hrms.common.exception.ResourceNotFoundException;
import com.priyex.hrms.common.response.PagedResponse;
import com.priyex.hrms.employee.dto.CreateEmployeeRequest;
import com.priyex.hrms.employee.mapper.EmployeeMapper;
import com.priyex.hrms.employee.model.Employee;
import com.priyex.hrms.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;

    @Override
    public PagedResponse<Employee> getEmployees(Long companyId, String query, Long departmentId, String status, int page, int size) {
        int offset = (page - 1) * size;
        List<Employee> items = employeeMapper.searchEmployees(companyId, query, departmentId, status, offset, size);
        long totalItems = employeeMapper.countEmployees(companyId, query, departmentId, status);
        return PagedResponse.of(items, page, size, totalItems);
    }

    @Override
    public Employee getEmployeeById(Long companyId, Long id) {
        return employeeMapper.findById(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }

    @Override
    public Employee getMyProfile(Long companyId, Long userId, Long employeeId) {
        if (employeeId != null) {
            java.util.Optional<Employee> byId = employeeMapper.findById(employeeId, companyId);
            if (byId.isPresent()) return byId.get();
        }
        if (userId != null) {
            java.util.Optional<Employee> byUserId = employeeMapper.findByUserId(userId, companyId);
            if (byUserId.isPresent()) return byUserId.get();
        }
        return employeeMapper.findById(1L, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "companyId", companyId));
    }

    @Override
    @Transactional
    public Employee updateMyProfile(Long companyId, Long userId, Long employeeId, com.priyex.hrms.employee.dto.UpdateSelfProfileRequest req) {
        Employee current = getMyProfile(companyId, userId, employeeId);
        employeeMapper.updateSelfProfile(
                current.getId(),
                companyId,
                req.getPhotoUrl(),
                req.getPersonalPhone(),
                req.getAddressLine1(),
                req.getCity(),
                req.getState(),
                req.getPostalCode()
        );
        return employeeMapper.findById(current.getId(), companyId).orElse(current);
    }

    @Override
    @Transactional
    public Employee createEmployee(Long companyId, Long actorUserId, CreateEmployeeRequest req) {
        String empCode = employeeMapper.generateEmployeeCode(companyId);
        if (empCode == null || empCode.isBlank()) {
            empCode = "EMP-1001";
        }

        Employee emp = Employee.builder()
                .companyId(companyId)
                .employeeCode(empCode)
                .firstName(req.getFirstName())
                .middleName(req.getMiddleName())
                .lastName(req.getLastName())
                .workEmail(req.getWorkEmail())
                .personalPhone(req.getPersonalPhone())
                .workPhone(req.getWorkPhone())
                .departmentId(req.getDepartmentId())
                .designationId(req.getDesignationId())
                .branchId(req.getBranchId())
                .employmentType(req.getEmploymentType() != null ? req.getEmploymentType() : "FULL_TIME")
                .joiningDate(req.getJoiningDate())
                .status(req.getStatus() != null ? req.getStatus() : "ACTIVE")
                .createdBy(actorUserId)
                .updatedBy(actorUserId)
                .build();

        employeeMapper.insert(emp);
        return getEmployeeById(companyId, emp.getId());
    }

    @Override
    @Transactional
    public Employee updateEmployee(Long companyId, Long id, CreateEmployeeRequest req) {
        Employee existing = getEmployeeById(companyId, id);
        existing.setFirstName(req.getFirstName());
        existing.setLastName(req.getLastName());
        existing.setWorkEmail(req.getWorkEmail());
        existing.setPersonalPhone(req.getPersonalPhone());
        existing.setDepartmentId(req.getDepartmentId());
        existing.setDesignationId(req.getDesignationId());
        existing.setEmploymentType(req.getEmploymentType());
        existing.setStatus(req.getStatus());

        employeeMapper.update(existing);
        return getEmployeeById(companyId, id);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long companyId, Long id) {
        getEmployeeById(companyId, id);
        employeeMapper.softDelete(id, companyId);
    }

    @Override
    public String exportEmployeesCsv(Long companyId) {
        List<Employee> list = employeeMapper.searchEmployees(companyId, null, null, null, 0, 10000);
        StringBuilder sb = new StringBuilder();
        sb.append("Employee Code,First Name,Last Name,Email,Phone,Department,Designation,Status,Joining Date\n");
        for (Employee e : list) {
            sb.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                    e.getEmployeeCode(),
                    e.getFirstName(),
                    e.getLastName(),
                    e.getWorkEmail(),
                    e.getPersonalPhone() != null ? e.getPersonalPhone() : "",
                    e.getDepartmentName() != null ? e.getDepartmentName() : "",
                    e.getDesignationName() != null ? e.getDesignationName() : "",
                    e.getStatus(),
                    e.getJoiningDate() != null ? e.getJoiningDate().toString() : ""
            ));
        }
        return sb.toString();
    }
}
