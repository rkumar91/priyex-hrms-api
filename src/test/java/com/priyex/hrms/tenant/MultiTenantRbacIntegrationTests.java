package com.priyex.hrms.tenant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.priyex.hrms.auth.dto.LoginRequest;
import com.priyex.hrms.organization.dto.OnboardClientRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SuppressWarnings("null")
public class MultiTenantRbacIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String obtainAccessToken(String email, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(root.get("success").asBoolean());
        return root.get("data").get("accessToken").asText();
    }

    @Test
    @DisplayName("1. Auto-detect organization workspace from registered corporate email domain")
    void testDomainDetection() throws Exception {
        // TCS domain detection
        MvcResult resTcs = mockMvc.perform(get("/api/v1/tenant/detect-domain")
                        .param("email", "employee@tcs.com"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode tcsNode = objectMapper.readTree(resTcs.getResponse().getContentAsString()).get("data");
        assertTrue(tcsNode.get("identified").asBoolean(), "TCS domain should be identified");
        assertEquals("TCS", tcsNode.get("company").get("code").asText());

        // Wipro domain detection
        MvcResult resWipro = mockMvc.perform(get("/api/v1/tenant/detect-domain")
                        .param("domain", "wipro.com"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode wiproNode = objectMapper.readTree(resWipro.getResponse().getContentAsString()).get("data");
        assertTrue(wiproNode.get("identified").asBoolean(), "Wipro domain should be identified");
        assertEquals("WIPRO", wiproNode.get("company").get("code").asText());

        // Infosys domain detection
        MvcResult resInfy = mockMvc.perform(get("/api/v1/tenant/detect-domain")
                        .param("email", "manager@infosys.com"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode infyNode = objectMapper.readTree(resInfy.getResponse().getContentAsString()).get("data");
        assertTrue(infyNode.get("identified").asBoolean(), "Infosys domain should be identified");
        assertEquals("INFOSYS", infyNode.get("company").get("code").asText());

        // Public/shared domain detection (Gmail)
        MvcResult resGmail = mockMvc.perform(get("/api/v1/tenant/detect-domain")
                        .param("email", "john.doe@gmail.com"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode gmailNode = objectMapper.readTree(resGmail.getResponse().getContentAsString()).get("data");
        assertFalse(gmailNode.get("identified").asBoolean(), "Public shared domain should not map exclusively to one tenant");
        assertTrue(gmailNode.get("shared").asBoolean(), "Gmail should be flagged as shared");

        // Non-existent random domain
        MvcResult resRandom = mockMvc.perform(get("/api/v1/tenant/detect-domain")
                        .param("domain", "nonexistentdomain999888.com"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode randomNode = objectMapper.readTree(resRandom.getResponse().getContentAsString()).get("data");
        assertFalse(randomNode.get("identified").asBoolean());
    }

    @Test
    @DisplayName("2. Multi-tenant login without manual company selection automatically resolves tenant & roles")
    void testMultiTenantLoginAutoResolution() throws Exception {
        // TCS Admin Login
        LoginRequest tcsAdminReq = new LoginRequest();
        tcsAdminReq.setEmail("admin@tcs.com");
        tcsAdminReq.setPassword("Admin@123");

        MvcResult tcsResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tcsAdminReq)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode tcsData = objectMapper.readTree(tcsResult.getResponse().getContentAsString()).get("data");
        assertNotNull(tcsData.get("accessToken").asText());
        assertEquals(2L, tcsData.get("user").get("companyId").asLong());
        assertEquals("TCS", tcsData.get("user").get("company").get("code").asText());
        assertTrue(tcsData.get("user").get("roles").toString().contains("ADMIN"));

        // Wipro Manager Login
        LoginRequest wiproMgrReq = new LoginRequest();
        wiproMgrReq.setEmail("manager@wipro.com");
        wiproMgrReq.setPassword("Admin@123");

        MvcResult wiproResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wiproMgrReq)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode wiproData = objectMapper.readTree(wiproResult.getResponse().getContentAsString()).get("data");
        assertEquals(3L, wiproData.get("user").get("companyId").asLong());
        assertEquals("WIPRO", wiproData.get("user").get("company").get("code").asText());
        assertTrue(wiproData.get("user").get("roles").toString().contains("MANAGER"));

        // Infosys Employee Login
        LoginRequest infyEmpReq = new LoginRequest();
        infyEmpReq.setEmail("employee@infosys.com");
        infyEmpReq.setPassword("Admin@123");

        MvcResult infyResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(infyEmpReq)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode infyData = objectMapper.readTree(infyResult.getResponse().getContentAsString()).get("data");
        assertEquals(4L, infyData.get("user").get("companyId").asLong());
        assertEquals("INFOSYS", infyData.get("user").get("company").get("code").asText());
        assertTrue(infyData.get("user").get("roles").toString().contains("EMPLOYEE"));

        // Platform Super Admin Login
        LoginRequest saReq = new LoginRequest();
        saReq.setEmail("superadmin@priyex.com");
        saReq.setPassword("Admin@123");

        MvcResult saResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saReq)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode saData = objectMapper.readTree(saResult.getResponse().getContentAsString()).get("data");
        assertTrue(saData.get("user").get("roles").toString().contains("SUPER_ADMIN"));
    }

    @Test
    @DisplayName("3. Super Admin exclusivity for Client Onboarding module (RBAC validation)")
    void testSuperAdminClientOnboardingRbac() throws Exception {
        String superAdminToken = obtainAccessToken("superadmin@priyex.com", "Admin@123");
        String tcsAdminToken = obtainAccessToken("admin@tcs.com", "Admin@123");
        String infyEmpToken = obtainAccessToken("employee@infosys.com", "Admin@123");

        // Super Admin can list all clients
        mockMvc.perform(get("/api/v1/super-admin/clients")
                        .header("Authorization", "Bearer " + superAdminToken))
                .andExpect(status().isOk());

        // Client Admin (TCS) is forbidden (403) from Super-Admin Client Onboarding
        mockMvc.perform(get("/api/v1/super-admin/clients")
                        .header("Authorization", "Bearer " + tcsAdminToken))
                .andExpect(status().isForbidden());

        // Client Employee (Infosys) is forbidden (403)
        mockMvc.perform(get("/api/v1/super-admin/clients")
                        .header("Authorization", "Bearer " + infyEmpToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("4. Cross-tenant isolation prevents unauthorized company tampering via headers")
    void testCrossTenantIsolation() throws Exception {
        String tcsEmpToken = obtainAccessToken("employee@tcs.com", "Admin@123");

        // Regular TCS user attempts to pass X-Company-Id: 3 (Wipro)
        // Backend must ignore the untrusted cross-tenant switch and enforce user's true company (Company 2)
        MvcResult res = mockMvc.perform(get("/api/v1/tenant/policies")
                        .header("Authorization", "Bearer " + tcsEmpToken)
                        .header("X-Company-Id", "3")
                        .param("companyId", "3"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode policies = objectMapper.readTree(res.getResponse().getContentAsString()).get("data");
        assertEquals(2L, policies.get("companyId").asLong(), "TCS user must only receive TCS policies, not Wipro policies");
    }

    @Test
    @DisplayName("5. Super Admin can onboard new client organization and manage lifecycle")
    void testClientOnboardingLifecycle() throws Exception {
        String superAdminToken = obtainAccessToken("superadmin@priyex.com", "Admin@123");
        String uniqueCode = "ACME" + (System.currentTimeMillis() % 10000);
        String uniqueEmail = "admin@" + uniqueCode.toLowerCase() + ".com";

        OnboardClientRequest req = OnboardClientRequest.builder()
                .code(uniqueCode)
                .legalName("Acme Global Solutions Limited")
                .brandName("Acme Global")
                .industry("Manufacturing & IT")
                .domains(List.of(uniqueCode.toLowerCase() + ".com"))
                .adminEmail(uniqueEmail)
                .adminName("Alexander Acme")
                .adminPassword("Admin@123")
                .city("Hyderabad")
                .state("Telangana")
                .country("India")
                .build();

        // 1. Onboard Client
        MvcResult onboardResult = mockMvc.perform(post("/api/v1/super-admin/clients")
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode clientData = objectMapper.readTree(onboardResult.getResponse().getContentAsString()).get("data");
        Long newClientId = clientData.get("id").asLong();
        assertEquals(uniqueCode, clientData.get("code").asText());

        // 2. Verify domain detection immediately detects new workspace
        MvcResult domainRes = mockMvc.perform(get("/api/v1/tenant/detect-domain")
                        .param("email", "john@" + uniqueCode.toLowerCase() + ".com"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode detected = objectMapper.readTree(domainRes.getResponse().getContentAsString()).get("data");
        assertTrue(detected.get("identified").asBoolean());
        assertEquals(uniqueCode, detected.get("company").get("code").asText());

        // 3. Newly created Admin can login
        String newAdminToken = obtainAccessToken(uniqueEmail, "Admin@123");
        assertNotNull(newAdminToken);

        // 4. Super Admin deactivates client organization
        mockMvc.perform(put("/api/v1/super-admin/clients/" + newClientId + "/status")
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("isActive", false))))
                .andExpect(status().isOk());

        // 5. Deactivated client login is rejected
        LoginRequest deactLogin = new LoginRequest();
        deactLogin.setEmail(uniqueEmail);
        deactLogin.setPassword("Admin@123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deactLogin)))
                .andExpect(status().isUnauthorized());
    }
}
