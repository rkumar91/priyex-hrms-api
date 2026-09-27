package com.priyex.hrms.employee.service.impl;

import com.priyex.hrms.common.exception.ResourceNotFoundException;
import com.priyex.hrms.common.response.PagedResponse;
import com.priyex.hrms.employee.dto.CreateEmployeeRequest;
import com.priyex.hrms.employee.mapper.EmployeeDocumentMapper;
import com.priyex.hrms.employee.mapper.EmployeeMapper;
import com.priyex.hrms.employee.model.Employee;
import com.priyex.hrms.employee.model.EmployeeDocument;
import com.priyex.hrms.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final EmployeeDocumentMapper employeeDocumentMapper;

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
        employeeMapper.updateSelfProfile(current.getId(), companyId, req);
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
                .addressLine1(req.getAddressLine1())
                .addressLine2(req.getAddressLine2())
                .city(req.getCity())
                .state(req.getState())
                .postalCode(req.getPostalCode())
                .country(req.getCountry() != null ? req.getCountry() : "India")
                .permanentAddressLine1(req.getPermanentAddressLine1())
                .permanentAddressLine2(req.getPermanentAddressLine2())
                .permanentCity(req.getPermanentCity())
                .permanentState(req.getPermanentState())
                .permanentPostalCode(req.getPermanentPostalCode())
                .permanentCountry(req.getPermanentCountry() != null ? req.getPermanentCountry() : "India")
                .bankName(req.getBankName())
                .bankBranch(req.getBankBranch())
                .bankAccountNumber(req.getBankAccountNumber())
                .bankIfsc(req.getBankIfsc())
                .bankAccountType(req.getBankAccountType() != null ? req.getBankAccountType() : "SALARY")
                .pfNumber(req.getPfNumber())
                .uanNumber(req.getUanNumber())
                .esiNumber(req.getEsiNumber())
                .panNumber(req.getPanNumber())
                .aadhaarNumber(req.getAadhaarNumber())
                .pfNomineeName(req.getPfNomineeName())
                .pfNomineeRelationship(req.getPfNomineeRelationship())
                .emergencyContactName(req.getEmergencyContactName())
                .emergencyContactRelationship(req.getEmergencyContactRelationship())
                .emergencyContactPhone(req.getEmergencyContactPhone())
                .dateOfBirth(req.getDateOfBirth())
                .gender(req.getGender())
                .bloodGroup(req.getBloodGroup())
                .maritalStatus(req.getMaritalStatus())
                .workLocation(req.getWorkLocation() != null ? req.getWorkLocation() : "Bangalore HQ")
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
        existing.setAddressLine1(req.getAddressLine1());
        existing.setAddressLine2(req.getAddressLine2());
        existing.setCity(req.getCity());
        existing.setState(req.getState());
        existing.setPostalCode(req.getPostalCode());
        existing.setCountry(req.getCountry());
        existing.setPermanentAddressLine1(req.getPermanentAddressLine1());
        existing.setPermanentAddressLine2(req.getPermanentAddressLine2());
        existing.setPermanentCity(req.getPermanentCity());
        existing.setPermanentState(req.getPermanentState());
        existing.setPermanentPostalCode(req.getPermanentPostalCode());
        existing.setPermanentCountry(req.getPermanentCountry());
        existing.setBankName(req.getBankName());
        existing.setBankBranch(req.getBankBranch());
        existing.setBankAccountNumber(req.getBankAccountNumber());
        existing.setBankIfsc(req.getBankIfsc());
        existing.setBankAccountType(req.getBankAccountType());
        existing.setPfNumber(req.getPfNumber());
        existing.setUanNumber(req.getUanNumber());
        existing.setEsiNumber(req.getEsiNumber());
        existing.setPanNumber(req.getPanNumber());
        existing.setAadhaarNumber(req.getAadhaarNumber());
        existing.setPfNomineeName(req.getPfNomineeName());
        existing.setPfNomineeRelationship(req.getPfNomineeRelationship());
        existing.setEmergencyContactName(req.getEmergencyContactName());
        existing.setEmergencyContactRelationship(req.getEmergencyContactRelationship());
        existing.setEmergencyContactPhone(req.getEmergencyContactPhone());
        existing.setDateOfBirth(req.getDateOfBirth());
        existing.setGender(req.getGender());
        existing.setBloodGroup(req.getBloodGroup());
        existing.setMaritalStatus(req.getMaritalStatus());
        existing.setWorkLocation(req.getWorkLocation());

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

    @Override
    public List<EmployeeDocument> getDocuments(Long employeeId) {
        return employeeDocumentMapper.findByEmployeeId(employeeId);
    }

    @Override
    @Transactional
    public EmployeeDocument uploadDocument(Long employeeId, Long actorUserId, com.priyex.hrms.employee.dto.UploadDocumentRequest req) {
        EmployeeDocument doc = EmployeeDocument.builder()
                .employeeId(employeeId)
                .documentType(req.getDocumentType())
                .documentName(req.getDocumentName())
                .fileData(req.getFileData())
                .mimeType(req.getMimeType() != null ? req.getMimeType() : "application/pdf")
                .fileSize(req.getFileSize() != null ? req.getFileSize() : 0L)
                .verified(false)
                .createdBy(actorUserId)
                .build();
        employeeDocumentMapper.insert(doc);
        return doc;
    }

    @Override
    @Transactional
    public void deleteDocument(Long employeeId, Long documentId) {
        employeeDocumentMapper.delete(documentId, employeeId);
    }
}
