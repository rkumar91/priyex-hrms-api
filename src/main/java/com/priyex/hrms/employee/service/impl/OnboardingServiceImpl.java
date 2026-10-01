package com.priyex.hrms.employee.service.impl;

import com.priyex.hrms.auth.mapper.UserMapper;
import com.priyex.hrms.auth.model.User;
import com.priyex.hrms.common.email.EmailService;
import com.priyex.hrms.common.exception.BadRequestException;
import com.priyex.hrms.common.exception.ResourceNotFoundException;
import com.priyex.hrms.employee.dto.InitiateOnboardingRequest;
import com.priyex.hrms.employee.dto.InitiateOnboardingResponse;
import com.priyex.hrms.employee.dto.PendingOnboardingDto;
import com.priyex.hrms.employee.dto.SubmitOnboardingRequest;
import com.priyex.hrms.employee.mapper.EmployeeDocumentMapper;
import com.priyex.hrms.employee.mapper.EmployeeMapper;
import com.priyex.hrms.employee.model.Employee;
import com.priyex.hrms.employee.model.EmployeeDocument;
import com.priyex.hrms.employee.service.OnboardingService;
import com.priyex.hrms.notification.mapper.AnnouncementMapper;
import com.priyex.hrms.notification.model.Announcement;
import com.priyex.hrms.organization.dto.DepartmentDTO;
import com.priyex.hrms.organization.mapper.DepartmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingServiceImpl implements OnboardingService {

    private final EmployeeMapper employeeMapper;
    private final UserMapper userMapper;
    private final EmployeeDocumentMapper employeeDocumentMapper;
    private final DepartmentMapper departmentMapper;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AnnouncementMapper announcementMapper;

    @Override
    @Transactional
    public InitiateOnboardingResponse initiateOnboarding(Long companyId, Long actorId, InitiateOnboardingRequest req, String baseUrl) {
        String workEmail = req.getWorkEmail().trim().toLowerCase();

        // Check if user or employee already exists with this email
        User existingUser = userMapper.findByEmail(workEmail);
        if (existingUser != null) {
            throw new BadRequestException("A user account with email " + workEmail + " already exists");
        }

        // Generate employee code
        String empCode = employeeMapper.generateEmployeeCode(companyId);
        if (empCode == null || empCode.isBlank()) {
            empCode = "EMP-" + (int)(1000 + Math.random() * 9000);
        }

        // Generate readable temporary password
        int randomPin = (int)(1000 + Math.random() * 9000);
        String tempPassword = "Priyex@" + randomPin;

        // Build Employee with status ONBOARDING
        Employee emp = Employee.builder()
                .companyId(companyId)
                .employeeCode(empCode)
                .firstName(req.getFirstName().trim())
                .middleName(req.getMiddleName() != null ? req.getMiddleName().trim() : null)
                .lastName(req.getLastName().trim())
                .workEmail(workEmail)
                .personalPhone(req.getPersonalPhone().trim())
                .departmentId(req.getDepartmentId())
                .designationId(req.getDesignationId())
                .branchId(req.getBranchId() != null ? req.getBranchId() : 1L)
                .employmentType(req.getEmploymentType() != null ? req.getEmploymentType() : "FULL_TIME")
                .joiningDate(req.getJoiningDate())
                .status("ONBOARDING")
                .createdBy(actorId)
                .updatedBy(actorId)
                .workLocation(req.getWorkLocation() != null ? req.getWorkLocation() : "Bangalore HQ")
                .annualCtc(req.getAnnualCtc() != null ? req.getAnnualCtc() : new BigDecimal("1200000.00"))
                .country("India")
                .permanentCountry("India")
                .build();

        employeeMapper.insert(emp);

        // Build and Insert User with must_change_password = true
        User user = User.builder()
                .companyId(companyId)
                .employeeId(emp.getId())
                .email(workEmail)
                .passwordHash(passwordEncoder.encode(tempPassword))
                .displayName(req.getFirstName().trim() + " " + req.getLastName().trim())
                .phone(req.getPersonalPhone().trim())
                .active(true)
                .mustChangePassword(true)
                .createdBy(actorId)
                .build();

        userMapper.insert(user);

        // Assign EMPLOYEE role
        Long roleId = userMapper.findRoleIdByName("EMPLOYEE");
        if (roleId != null) {
            userMapper.insertUserRole(user.getId(), roleId, actorId);
        }

        // Link user_id on employee
        employeeMapper.linkUserId(emp.getId(), user.getId(), companyId);

        // Send onboarding invitation email from tech.priyex@gmail.com
        String loginUrl = (baseUrl != null && !baseUrl.isBlank()) ? baseUrl + "/login" : "http://localhost:3000/login";
        String fullName = req.getFirstName().trim() + " " + req.getLastName().trim();
        emailService.sendOnboardingInvite(workEmail, fullName, tempPassword, loginUrl);

        return InitiateOnboardingResponse.builder()
                .employeeId(emp.getId())
                .userId(user.getId())
                .employeeCode(empCode)
                .fullName(fullName)
                .workEmail(workEmail)
                .tempPassword(tempPassword)
                .inviteUrl(loginUrl)
                .status("ONBOARDING")
                .message("Onboarding initiated and invitation email dispatched from tech.priyex@gmail.com")
                .build();
    }

    @Override
    @Transactional
    public Employee submitOnboarding(Long companyId, Long userId, Long employeeId, SubmitOnboardingRequest req) {
        Employee employee = null;
        if (employeeId != null) {
            employee = employeeMapper.findById(employeeId, companyId).orElse(null);
        }
        if (employee == null && userId != null) {
            employee = employeeMapper.findByUserId(userId, companyId).orElse(null);
        }
        if (employee == null) {
            throw new ResourceNotFoundException("Employee", "userId", userId);
        }

        // Update Personal Info
        if (req.getDateOfBirth() != null) employee.setDateOfBirth(req.getDateOfBirth());
        if (req.getGender() != null) employee.setGender(req.getGender());
        if (req.getBloodGroup() != null) employee.setBloodGroup(req.getBloodGroup());
        if (req.getMaritalStatus() != null) employee.setMaritalStatus(req.getMaritalStatus());
        if (req.getEmergencyContactName() != null) employee.setEmergencyContactName(req.getEmergencyContactName());
        if (req.getEmergencyContactRelationship() != null) employee.setEmergencyContactRelationship(req.getEmergencyContactRelationship());
        if (req.getEmergencyContactPhone() != null) employee.setEmergencyContactPhone(req.getEmergencyContactPhone());

        // Update Address Info
        if (req.getAddressLine1() != null) employee.setAddressLine1(req.getAddressLine1());
        if (req.getAddressLine2() != null) employee.setAddressLine2(req.getAddressLine2());
        if (req.getCity() != null) employee.setCity(req.getCity());
        if (req.getState() != null) employee.setState(req.getState());
        if (req.getPostalCode() != null) employee.setPostalCode(req.getPostalCode());
        if (req.getCountry() != null) employee.setCountry(req.getCountry());

        if (req.getPermanentAddressLine1() != null) employee.setPermanentAddressLine1(req.getPermanentAddressLine1());
        if (req.getPermanentAddressLine2() != null) employee.setPermanentAddressLine2(req.getPermanentAddressLine2());
        if (req.getPermanentCity() != null) employee.setPermanentCity(req.getPermanentCity());
        if (req.getPermanentState() != null) employee.setPermanentState(req.getPermanentState());
        if (req.getPermanentPostalCode() != null) employee.setPermanentPostalCode(req.getPermanentPostalCode());
        if (req.getPermanentCountry() != null) employee.setPermanentCountry(req.getPermanentCountry());

        // Update Bank & Statutory Info
        if (req.getBankName() != null) employee.setBankName(req.getBankName());
        if (req.getBankBranch() != null) employee.setBankBranch(req.getBankBranch());
        if (req.getBankAccountNumber() != null) employee.setBankAccountNumber(req.getBankAccountNumber());
        if (req.getBankIfsc() != null) employee.setBankIfsc(req.getBankIfsc());
        if (req.getBankAccountType() != null) employee.setBankAccountType(req.getBankAccountType());

        if (req.getPanNumber() != null) employee.setPanNumber(req.getPanNumber());
        if (req.getAadhaarNumber() != null) employee.setAadhaarNumber(req.getAadhaarNumber());
        if (req.getPfNumber() != null) employee.setPfNumber(req.getPfNumber());
        if (req.getUanNumber() != null) employee.setUanNumber(req.getUanNumber());
        if (req.getPfNomineeName() != null) employee.setPfNomineeName(req.getPfNomineeName());
        if (req.getPfNomineeRelationship() != null) employee.setPfNomineeRelationship(req.getPfNomineeRelationship());

        // Set status to PENDING_APPROVAL
        employee.setStatus("PENDING_APPROVAL");
        employeeMapper.update(employee);

        return employee;
    }

    @Override
    public List<PendingOnboardingDto> getPendingOnboardings(Long companyId) {
        List<Employee> all = employeeMapper.searchEmployees(companyId, null, null, null, 0, 100);
        List<DepartmentDTO> departments = departmentMapper.findAllByCompanyId(companyId);
        Map<Long, String> deptMap = new HashMap<>();
        for (DepartmentDTO d : departments) {
            deptMap.put(d.getId(), d.getName());
        }

        List<PendingOnboardingDto> pending = new ArrayList<>();
        for (Employee e : all) {
            if ("ONBOARDING".equalsIgnoreCase(e.getStatus()) || "PENDING_APPROVAL".equalsIgnoreCase(e.getStatus())) {
                List<EmployeeDocument> docs = employeeDocumentMapper.findByEmployeeId(e.getId());
                pending.add(PendingOnboardingDto.builder()
                        .id(e.getId())
                        .employeeCode(e.getEmployeeCode())
                        .firstName(e.getFirstName())
                        .lastName(e.getLastName())
                        .workEmail(e.getWorkEmail())
                        .personalPhone(e.getPersonalPhone())
                        .departmentId(e.getDepartmentId())
                        .departmentName(deptMap.getOrDefault(e.getDepartmentId(), "Engineering & Technology"))
                        .designationId(e.getDesignationId())
                        .designationName(e.getDesignationName() != null ? e.getDesignationName() : "Software Engineer")
                        .joiningDate(e.getJoiningDate())
                        .status(e.getStatus())
                        .documentCount(docs != null ? docs.size() : 0)
                        .isSubmitted("PENDING_APPROVAL".equalsIgnoreCase(e.getStatus()))
                        .panNumber(e.getPanNumber())
                        .aadhaarNumber(e.getAadhaarNumber())
                        .bankAccountNumber(e.getBankAccountNumber())
                        .bankIfsc(e.getBankIfsc())
                        .build());
            }
        }
        return pending;
    }

    @Override
    @Transactional
    public Employee approveOnboarding(Long companyId, Long actorId, Long employeeId, String remarks) {
        Employee employee = employeeMapper.findById(employeeId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        employee.setStatus("ACTIVE");
        employeeMapper.update(employee);

        // Send approval email notification from tech.priyex@gmail.com
        String fullName = employee.getFirstName() + " " + employee.getLastName();
        emailService.sendOnboardingApprovalNotification(employee.getWorkEmail(), fullName);

        // Publish welcome announcement to all users
        Announcement welcomeAnnouncement = Announcement.builder()
                .companyId(companyId)
                .title("Welcome " + fullName + " to Priyex People!")
                .body(fullName + " (" + employee.getEmployeeCode() + ") has completed onboarding and officially joined our team. Let's extend a warm welcome!")
                .priority("NORMAL")
                .targetAudience("ALL")
                .isPublished(true)
                .requiresAck(false)
                .createdBy(actorId)
                .build();
        announcementMapper.insert(welcomeAnnouncement);

        return employee;
    }

    @Override
    @Transactional
    public Employee requestRevisions(Long companyId, Long actorId, Long employeeId, String feedbackNotes) {
        Employee employee = employeeMapper.findById(employeeId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        employee.setStatus("ONBOARDING");
        employeeMapper.update(employee);

        log.info("HR requested onboarding revisions for employee {}: {}", employeeId, feedbackNotes);
        return employee;
    }
}
