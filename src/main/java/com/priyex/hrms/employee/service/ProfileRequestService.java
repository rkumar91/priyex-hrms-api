package com.priyex.hrms.employee.service;

import com.priyex.hrms.employee.dto.CreateProfileRequest;
import com.priyex.hrms.employee.model.ProfileRequest;

import java.util.List;

public interface ProfileRequestService {

    ProfileRequest submitRequest(Long companyId, Long employeeId, CreateProfileRequest request);

    List<ProfileRequest> getRequests(Long companyId, Long employeeId, String status);

    ProfileRequest approveRequest(Long companyId, Long requestId, Long reviewerUserId, String notes);

    ProfileRequest rejectRequest(Long companyId, Long requestId, Long reviewerUserId, String notes);
}
