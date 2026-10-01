package com.priyex.hrms.employee.service.impl;

import com.priyex.hrms.auth.mapper.UserMapper;
import com.priyex.hrms.common.exception.BadRequestException;
import com.priyex.hrms.common.exception.ResourceNotFoundException;
import com.priyex.hrms.employee.dto.CreateProfileRequest;
import com.priyex.hrms.employee.mapper.EmployeeMapper;
import com.priyex.hrms.employee.mapper.ProfileRequestMapper;
import com.priyex.hrms.employee.model.Employee;
import com.priyex.hrms.employee.model.ProfileRequest;
import com.priyex.hrms.employee.service.ProfileRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileRequestServiceImpl implements ProfileRequestService {

    private final ProfileRequestMapper profileRequestMapper;
    private final EmployeeMapper employeeMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public ProfileRequest submitRequest(Long companyId, Long employeeId, CreateProfileRequest req) {
        Employee employee = employeeMapper.findById(employeeId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        String currentVal = req.getCurrentValue();
        if (currentVal == null || currentVal.isBlank()) {
            if ("BANK_ACCOUNT".equalsIgnoreCase(req.getRequestType())) {
                currentVal = employee.getBankAccountNumber() != null ? employee.getBankAccountNumber() : "Not set";
            } else if ("CONTACT_NAME".equalsIgnoreCase(req.getRequestType())) {
                currentVal = employee.getFirstName() + " " + employee.getLastName();
            } else if ("EMAIL".equalsIgnoreCase(req.getRequestType())) {
                currentVal = employee.getWorkEmail();
            }
        }

        ProfileRequest record = ProfileRequest.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .requestType(req.getRequestType().toUpperCase())
                .fieldName(req.getFieldName())
                .currentValue(currentVal)
                .requestedValue(req.getRequestedValue())
                .reason(req.getReason())
                .status("PENDING")
                .build();

        profileRequestMapper.insert(record);
        return profileRequestMapper.findById(record.getId(), companyId)
                .orElse(record);
    }

    @Override
    public List<ProfileRequest> getRequests(Long companyId, Long employeeId, String status) {
        return profileRequestMapper.findRequests(companyId, employeeId, status);
    }

    @Override
    @Transactional
    public ProfileRequest approveRequest(Long companyId, Long requestId, Long reviewerUserId, String notes) {
        ProfileRequest request = profileRequestMapper.findById(requestId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("ProfileRequest", "id", requestId));

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new BadRequestException("Request #" + requestId + " is already " + request.getStatus());
        }

        // Apply changes to employee profile
        String type = request.getRequestType();
        String requestedValue = request.getRequestedValue();
        Long empId = request.getEmployeeId();

        if (requestedValue != null && !requestedValue.isBlank()) {
            if ("BANK_ACCOUNT".equalsIgnoreCase(type)) {
                String safeValue = requestedValue.length() > 250 ? requestedValue.substring(0, 250) : requestedValue;
                employeeMapper.updateDirectField(empId, companyId, "bank_account_number", safeValue);
            } else if ("CONTACT_NAME".equalsIgnoreCase(type)) {
                String[] parts = requestedValue.trim().split("\\s+", 2);
                employeeMapper.updateDirectField(empId, companyId, "first_name", parts[0]);
                if (parts.length > 1) {
                    employeeMapper.updateDirectField(empId, companyId, "last_name", parts[1]);
                }
            } else if ("EMAIL".equalsIgnoreCase(type)) {
                employeeMapper.updateDirectField(empId, companyId, "work_email", requestedValue.trim());
            }
        }

        // Resolve safe reviewerId to prevent foreign key constraint violations
        Long safeReviewerId = resolveSafeReviewerId(reviewerUserId);

        profileRequestMapper.updateStatus(requestId, companyId, "APPROVED", safeReviewerId, notes);
        return profileRequestMapper.findById(requestId, companyId).orElse(request);
    }

    @Override
    @Transactional
    public ProfileRequest rejectRequest(Long companyId, Long requestId, Long reviewerUserId, String notes) {
        ProfileRequest request = profileRequestMapper.findById(requestId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("ProfileRequest", "id", requestId));

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new BadRequestException("Request #" + requestId + " is already " + request.getStatus());
        }

        // Resolve safe reviewerId to prevent foreign key constraint violations
        Long safeReviewerId = resolveSafeReviewerId(reviewerUserId);

        profileRequestMapper.updateStatus(requestId, companyId, "REJECTED", safeReviewerId, notes);
        return profileRequestMapper.findById(requestId, companyId).orElse(request);
    }

    private Long resolveSafeReviewerId(Long reviewerUserId) {
        if (reviewerUserId == null) {
            return null;
        }
        try {
            if (userMapper.findById(reviewerUserId) != null) {
                return reviewerUserId;
            }
        } catch (Exception e) {
            log.warn("Could not verify reviewer user id: {}", reviewerUserId, e);
        }
        return null;
    }
}
