package com.priyex.hrms.organization.service.impl;

import com.priyex.hrms.auth.mapper.UserMapper;
import com.priyex.hrms.auth.model.User;
import com.priyex.hrms.common.exception.BadRequestException;
import com.priyex.hrms.common.exception.ResourceNotFoundException;
import com.priyex.hrms.employee.mapper.EmployeeMapper;
import com.priyex.hrms.employee.model.Employee;
import com.priyex.hrms.organization.dto.ClientOverviewDto;
import com.priyex.hrms.organization.dto.CompanyDto;
import com.priyex.hrms.organization.dto.DomainDetectionResult;
import com.priyex.hrms.organization.dto.OnboardClientRequest;
import com.priyex.hrms.organization.mapper.*;
import com.priyex.hrms.organization.model.CompanyDomain;
import com.priyex.hrms.organization.service.ClientOnboardingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClientOnboardingServiceImpl implements ClientOnboardingService {

    private static final Set<String> PUBLIC_SHARED_DOMAINS = Set.of(
            "gmail.com", "googlemail.com", "yahoo.com", "yahoo.co.in",
            "outlook.com", "hotmail.com", "live.com", "icloud.com", "proton.me", "aol.com"
    );

    private final CompanyMapper companyMapper;
    private final CompanyDomainMapper companyDomainMapper;
    private final BranchMapper branchMapper;
    private final DepartmentMapper departmentMapper;
    private final DesignationMapper designationMapper;
    private final CompanySettingMapper companySettingMapper;
    private final UserMapper userMapper;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    public ClientOnboardingServiceImpl(
            CompanyMapper companyMapper,
            CompanyDomainMapper companyDomainMapper,
            BranchMapper branchMapper,
            DepartmentMapper departmentMapper,
            DesignationMapper designationMapper,
            CompanySettingMapper companySettingMapper,
            UserMapper userMapper,
            EmployeeMapper employeeMapper,
            PasswordEncoder passwordEncoder
    ) {
        super();
        this.companyMapper = companyMapper;
        this.companyDomainMapper = companyDomainMapper;
        this.branchMapper = branchMapper;
        this.departmentMapper = departmentMapper;
        this.designationMapper = designationMapper;
        this.companySettingMapper = companySettingMapper;
        this.userMapper = userMapper;
        this.employeeMapper = employeeMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientOverviewDto> getAllClients() {
        List<CompanyDto> companies = companyMapper.findAllClients();
        return companies.stream()
                .map(this::mapToOverview)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClientOverviewDto getClientById(Long id) {
        CompanyDto company = companyMapper.findById(id);
        if (company == null) {
            throw new ResourceNotFoundException("Client organization not found with ID: " + id);
        }
        return mapToOverview(company);
    }

    @Override
    @Transactional
    public ClientOverviewDto onboardClient(OnboardClientRequest req, Long currentUserId) {
        String cleanCode = req.getCode().trim().toUpperCase();
        if (companyMapper.findByCode(cleanCode) != null) {
            throw new BadRequestException("Organization code '" + cleanCode + "' is already in use.");
        }

        String cleanAdminEmail = req.getAdminEmail().trim().toLowerCase();
        if (userMapper.findByEmail(cleanAdminEmail) != null) {
            throw new BadRequestException("A user with email '" + cleanAdminEmail + "' is already registered in the system.");
        }

        // 1. Create Company Entity
        CompanyDto company = CompanyDto.builder()
                .code(cleanCode)
                .legalName(req.getLegalName().trim())
                .brandName(req.getBrandName() != null && !req.getBrandName().isBlank() ? req.getBrandName().trim() : req.getLegalName().trim())
                .industry(req.getIndustry() != null ? req.getIndustry().trim() : "Technology & Services")
                .timezone("Asia/Kolkata")
                .currencyCode("INR")
                .phone(req.getPhone())
                .email(cleanAdminEmail)
                .city(req.getCity() != null ? req.getCity().trim() : "Bengaluru")
                .state(req.getState() != null ? req.getState().trim() : "Karnataka")
                .country(req.getCountry() != null ? req.getCountry().trim() : "India")
                .logoUrl(req.getLogoUrl() != null && !req.getLogoUrl().isBlank() ? req.getLogoUrl().trim() : null)
                .isActive(true)
                .build();

        companyMapper.insertCompany(company);
        Long companyId = company.getId();
        log.info("Onboarded new client organization: {} (ID: {})", company.getLegalName(), companyId);

        // 2. Register Client Email Domains
        List<String> inputDomains = req.getDomains() != null ? req.getDomains() : new ArrayList<>();
        // If domain not explicitly passed, extract from admin email
        if (inputDomains.isEmpty() && cleanAdminEmail.contains("@")) {
            String extracted = cleanAdminEmail.substring(cleanAdminEmail.indexOf('@') + 1).toLowerCase();
            if (!PUBLIC_SHARED_DOMAINS.contains(extracted)) {
                inputDomains.add(extracted);
            }
        }

        boolean isFirst = true;
        for (String dom : inputDomains) {
            if (dom != null && !dom.isBlank()) {
                String cleanDom = dom.trim().toLowerCase();
                CompanyDomain domainEntity = CompanyDomain.builder()
                        .companyId(companyId)
                        .domainName(cleanDom)
                        .isPrimary(isFirst)
                        .isVerified(true)
                        .isShared(PUBLIC_SHARED_DOMAINS.contains(cleanDom))
                        .build();
                companyDomainMapper.insert(domainEntity);
                isFirst = false;
            }
        }

        // 3. Seed Default Head Office Branch
        branchMapper.insertBranch(
                companyId,
                cleanCode + "-HQ",
                company.getBrandName() + " Head Office",
                company.getCity(),
                company.getState(),
                company.getCountry(),
                true
        );
        Long branchId = branchMapper.findDefaultBranchId(companyId);

        // 4. Seed Core Departments & Designations
        departmentMapper.insertDepartment(companyId, "MGMT", "Executive Management", "Leadership & Administration");
        departmentMapper.insertDepartment(companyId, "HR", "Human Resources", "People Operations");
        departmentMapper.insertDepartment(companyId, "ENG", "Engineering & Operations", "Core Business Delivery");

        designationMapper.insertDesignation(companyId, "Organization Administrator", 1, "Platform & Workspace Admin");
        designationMapper.insertDesignation(companyId, "Director", 2, "Senior Executive");
        designationMapper.insertDesignation(companyId, "Manager", 3, "Team Lead & Approver");
        designationMapper.insertDesignation(companyId, "Associate", 4, "Core Team Member");

        // 5. Seed Core Company Policy Settings
        companySettingMapper.upsert(companyId, "LEAVE_CALENDAR_CYCLE", "CALENDAR_YEAR", "Leave quota cycle: CALENDAR_YEAR");
        companySettingMapper.upsert(companyId, "ATTENDANCE_GRACE_MINS", "15", "Shift start grace minutes");
        companySettingMapper.upsert(companyId, "APPROVAL_TIER_COUNT", "2", "Number of approval tiers");
        companySettingMapper.upsert(companyId, "HALF_DAY_MIN_HOURS", "4.0", "Minimum hours for half day credit");
        companySettingMapper.upsert(companyId, "FULL_DAY_MIN_HOURS", "8.0", "Minimum hours for full day credit");

        // 6. Create Initial Admin User Account
        String rawPassword = req.getAdminPassword() != null && !req.getAdminPassword().isBlank()
                ? req.getAdminPassword()
                : "Admin@123";
        String passwordHash = passwordEncoder.encode(rawPassword);

        User adminUser = User.builder()
                .companyId(companyId)
                .email(cleanAdminEmail)
                .passwordHash(passwordHash)
                .displayName(req.getAdminName().trim())
                .active(true)
                .mustChangePassword(false)
                .createdBy(currentUserId)
                .build();
        userMapper.insert(adminUser);

        // Assign ADMIN role
        Long adminRoleId = userMapper.findRoleIdByName("ADMIN");
        if (adminRoleId == null) {
            adminRoleId = userMapper.findRoleIdByName("SUPER_ADMIN");
        }
        if (adminRoleId != null) {
            userMapper.insertUserRole(adminUser.getId(), adminRoleId, currentUserId);
        }

        // 7. Create Employee Record for Admin
        String[] nameParts = req.getAdminName().trim().split("\\s+", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "Admin";

        Employee adminEmployee = Employee.builder()
                .companyId(companyId)
                .employeeCode(cleanCode + "-001")
                .userId(adminUser.getId())
                .firstName(firstName)
                .lastName(lastName)
                .workEmail(cleanAdminEmail)
                .branchId(branchId)
                .employmentType("FULL_TIME")
                .joiningDate(LocalDate.now())
                .status("ACTIVE")
                .createdBy(currentUserId)
                .build();
        employeeMapper.insert(adminEmployee);

        // Link user to employee
        employeeMapper.linkUserId(adminEmployee.getId(), adminUser.getId(), companyId);

        log.info("Successfully provisioned admin account {} for organization {}", cleanAdminEmail, cleanCode);
        return mapToOverview(company);
    }

    @Override
    @Transactional
    public void toggleClientStatus(Long id, boolean isActive) {
        CompanyDto company = companyMapper.findById(id);
        if (company == null) {
            throw new ResourceNotFoundException("Client organization not found with ID: " + id);
        }
        companyMapper.updateCompanyStatus(id, isActive);
        log.info("Updated organization {} (ID: {}) active status to: {}", company.getCode(), id, isActive);
    }

    @Override
    @Transactional(readOnly = true)
    public DomainDetectionResult detectDomain(String emailOrDomain) {
        if (emailOrDomain == null || emailOrDomain.isBlank()) {
            return DomainDetectionResult.builder()
                    .identified(false)
                    .shared(false)
                    .message("No domain provided")
                    .build();
        }

        String target = emailOrDomain.trim().toLowerCase();
        if (target.contains("@")) {
            target = target.substring(target.indexOf('@') + 1).trim();
        }

        if (PUBLIC_SHARED_DOMAINS.contains(target)) {
            return DomainDetectionResult.builder()
                    .identified(false)
                    .shared(true)
                    .domain(target)
                    .message("Public shared email provider detected. Organization workspace will be identified via your registered user account.")
                    .build();
        }

        CompanyDomain domainRecord = companyDomainMapper.findByDomainName(target);
        if (domainRecord == null) {
            return DomainDetectionResult.builder()
                    .identified(false)
                    .shared(false)
                    .domain(target)
                    .message("Domain not pre-mapped to a specific workspace.")
                    .build();
        }

        if (Boolean.TRUE.equals(domainRecord.getIsShared())) {
            return DomainDetectionResult.builder()
                    .identified(false)
                    .shared(true)
                    .domain(target)
                    .message("Domain is shared across multiple accounts.")
                    .build();
        }

        CompanyDto company = companyMapper.findById(domainRecord.getCompanyId());
        if (company == null || !Boolean.TRUE.equals(company.getIsActive())) {
            return DomainDetectionResult.builder()
                    .identified(false)
                    .shared(false)
                    .domain(target)
                    .message("Organization workspace is currently inactive or not found.")
                    .build();
        }

        return DomainDetectionResult.builder()
                .identified(true)
                .shared(false)
                .domain(target)
                .company(company)
                .message("Mapped to " + company.getBrandName())
                .build();
    }

    @Override
    @Transactional
    public void addDomain(Long clientId, String domainName, boolean isPrimary) {
        if (domainName == null || domainName.isBlank()) {
            throw new BadRequestException("Domain name cannot be blank");
        }
        String cleanDom = domainName.trim().toLowerCase();
        if (cleanDom.contains("@")) {
            cleanDom = cleanDom.substring(cleanDom.indexOf('@') + 1).trim();
        }

        CompanyDto company = companyMapper.findById(clientId);
        if (company == null) {
            throw new ResourceNotFoundException("Client organization not found with ID: " + clientId);
        }

        CompanyDomain domainEntity = CompanyDomain.builder()
                .companyId(clientId)
                .domainName(cleanDom)
                .isPrimary(isPrimary)
                .isVerified(true)
                .isShared(PUBLIC_SHARED_DOMAINS.contains(cleanDom))
                .build();
        companyDomainMapper.insert(domainEntity);
        log.info("Associated domain {} to client organization {}", cleanDom, company.getCode());
    }

    @Override
    @Transactional
    public void removeDomain(Long clientId, Long domainId) {
        int deleted = companyDomainMapper.deleteByIdAndCompanyId(domainId, clientId);
        if (deleted == 0) {
            throw new ResourceNotFoundException("Domain mapping not found or already deleted");
        }
    }

    @Override
    @Transactional
    public void updateClientLogo(Long clientId, String logoUrl) {
        CompanyDto company = companyMapper.findById(clientId);
        if (company == null) {
            throw new ResourceNotFoundException("Client organization not found with ID: " + clientId);
        }
        companyMapper.updateCompanyLogo(clientId, logoUrl);
        log.info("Updated logo for organization {} (ID: {})", company.getCode(), clientId);
    }

    @SuppressWarnings("null")
    private ClientOverviewDto mapToOverview(CompanyDto company) {
        List<CompanyDomain> domainEntities = companyDomainMapper.findDomainsByCompanyId(company.getId());
        List<String> domains = domainEntities.stream()
                .map(CompanyDomain::getDomainName)
                .collect(Collectors.toList());

        int employeeCount = companyMapper.countEmployeesByCompanyId(company.getId());
        String adminEmail = companyMapper.findPrimaryAdminEmailByCompanyId(company.getId());

        String status = "ACTIVE";
        if (!Boolean.TRUE.equals(company.getIsActive())) {
            status = "INACTIVE";
        } else if (employeeCount == 0) {
            status = "SETUP_REQUIRED";
        }

        return ClientOverviewDto.builder()
                .id(company.getId())
                .code(company.getCode())
                .legalName(company.getLegalName())
                .brandName(company.getBrandName() != null ? company.getBrandName() : company.getLegalName())
                .industry(company.getIndustry())
                .timezone(company.getTimezone())
                .currencyCode(company.getCurrencyCode())
                .email(company.getEmail())
                .phone(company.getPhone())
                .city(company.getCity())
                .state(company.getState())
                .country(company.getCountry())
                .logoUrl(company.getLogoUrl())
                .isActive(company.getIsActive())
                .domains(domains)
                .employeeCount(employeeCount)
                .adminEmail(adminEmail != null ? adminEmail : company.getEmail())
                .onboardingStatus(status)
                .build();
    }
}
