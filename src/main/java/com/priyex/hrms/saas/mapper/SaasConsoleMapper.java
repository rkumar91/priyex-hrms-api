package com.priyex.hrms.saas.mapper;

import com.priyex.hrms.lead.model.LeadInquiry;
import com.priyex.hrms.saas.model.*;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface SaasConsoleMapper {

    @Select("""
        SELECT
            (SELECT COUNT(*) FROM lead_inquiries) AS totalLeads,
            (SELECT COUNT(*) FROM lead_inquiries WHERE status = 'NEW') AS newLeads,
            (SELECT COUNT(*) FROM lead_inquiries WHERE status = 'DEMO_SCHEDULED' OR inquiry_type = 'DEMO_REQUEST') AS demosScheduled,
            (SELECT COUNT(*) FROM companies WHERE is_active = TRUE) AS activeAccounts,
            (SELECT COUNT(*) FROM companies WHERE is_active = TRUE) AS activeTenants,
            (SELECT COUNT(*) FROM companies) AS totalTenants,
            (SELECT COUNT(*) FROM saas_subscriptions WHERE status = 'ACTIVE') AS totalSubscribers,
            (SELECT COUNT(*) FROM saas_subscriptions WHERE status = 'ACTIVE') AS totalSubscriptions,
            (SELECT COUNT(*) FROM employees WHERE status = 'ACTIVE') AS totalWorkforceEmployees,
            COALESCE((SELECT SUM(annual_contract_value) FROM saas_commercial_accounts WHERE lifecycle_status = 'ACTIVE'), 1370000.00) AS estimatedArr,
            COALESCE((SELECT SUM(annual_contract_value) FROM saas_commercial_accounts WHERE lifecycle_status = 'ACTIVE'), 1370000.00) AS activeArr,
            ROUND(COALESCE((SELECT SUM(annual_contract_value) FROM saas_commercial_accounts WHERE lifecycle_status = 'ACTIVE'), 1370000.00) / 12, 2) AS activeMrr,
            (SELECT COUNT(*) FROM saas_customer_requests WHERE status IN ('NEW', 'TRIAGED', 'IN_PROGRESS', 'OPEN')) AS openCustomerRequests,
            (SELECT COUNT(*) FROM lead_inquiries WHERE status = 'NEW' AND created_at < NOW() - INTERVAL '2 days') AS overdueFollowups,
            (SELECT COUNT(*) FROM saas_contracts WHERE billing_status != 'WAIVED') AS activeContractsCount,
            COALESCE((SELECT SUM(amount) FROM saas_contracts WHERE billing_status != 'WAIVED'), 0) AS totalContractValue,
            COALESCE((SELECT SUM(amount) FROM saas_contracts WHERE contract_type = 'ONE_TIME_SETUP' AND billing_status = 'PAID'), 0) AS setupRevenueTotal,
            COALESCE((SELECT SUM(amount) FROM saas_contracts WHERE contract_type = 'ANNUAL_MAINTENANCE' AND billing_status = 'PAID'), 0) AS amcRevenueTotal,
            COALESCE((SELECT SUM(total_amount) FROM saas_invoices WHERE status = 'PAID'), 0) AS totalCollectedRevenue,
            COALESCE((SELECT SUM(total_amount) FROM saas_invoices WHERE status IN ('ISSUED', 'OVERDUE')), 0) AS overdueReceivables
    """)
    SaasConsoleOverview getOverview();

    @Select("""
        SELECT
            COALESCE(a.id, c.id) AS id,
            COALESCE(a.account_ref, CONCAT('PRX-ACC-', c.code)) AS accountRef,
            COALESCE(a.account_name, c.brand_name, c.legal_name) AS accountName,
            c.legal_name AS legalName,
            COALESCE(a.billing_country, c.country, 'India') AS billingCountry,
            COALESCE(a.billing_currency, c.currency_code, 'INR') AS billingCurrency,
            COALESCE(a.business_domain, (SELECT domain_name FROM company_domains cd WHERE cd.company_id = c.id LIMIT 1), CONCAT(LOWER(c.code), '.com')) AS businessDomain,
            COALESCE(a.primary_contact_name, (SELECT CONCAT(first_name, ' ', last_name) FROM employees e WHERE e.company_id = c.id ORDER BY id ASC LIMIT 1), 'Client Administrator') AS primaryContactName,
            COALESCE(a.primary_contact_email, c.email) AS primaryContactEmail,
            COALESCE(a.primary_contact_phone, c.phone) AS primaryContactPhone,
            COALESCE(a.assigned_owner, 'tech.priyex@gmail.com') AS assignedOwner,
            CASE
                WHEN c.is_active = FALSE THEN 'TERMINATED'
                WHEN a.lifecycle_status IS NOT NULL THEN a.lifecycle_status
                ELSE 'ACTIVE'
            END AS lifecycleStatus,
            c.id AS linkedCompanyId,
            c.code AS linkedCompanyCode,
            c.code AS tenantSlug,
            c.industry AS industry,
            c.tax_id AS taxId,
            c.city AS city,
            c.state AS state,
            c.logo_url AS logoUrl,
            (SELECT COUNT(*) FROM employees e WHERE e.company_id = c.id) AS employeeCount,
            (SELECT COUNT(*) FROM employees e WHERE e.company_id = c.id AND e.status = 'ACTIVE') AS activeEmployeeCount,
            COALESCE(sub.seats_licensed, 25) AS licensedSeats,
            COALESCE(p.plan_name, 'Priyex Enterprise Growth') AS activePlanName,
            COALESCE(p.plan_code, 'GROWTH') AS activePlanCode,
            COALESCE(a.annual_contract_value, 250000.00) AS annualContractValue,
            a.drop_reason AS dropReason,
            a.drop_notes AS dropNotes,
            a.drop_date AS dropDate,
            COALESCE(a.created_at, c.created_at) AS createdAt,
            COALESCE(a.updated_at, c.updated_at) AS updatedAt
        FROM companies c
        LEFT JOIN saas_commercial_accounts a ON a.linked_company_id = c.id
        LEFT JOIN saas_subscriptions sub ON (sub.account_id = a.id AND sub.status = 'ACTIVE')
        LEFT JOIN saas_plans p ON sub.plan_id = p.id
        ORDER BY c.id ASC
    """)
    List<SaasCommercialAccount> findAllAccounts();

    @Insert("""
        INSERT INTO saas_commercial_accounts (
            account_ref, account_name, legal_name, billing_country, billing_currency,
            business_domain, primary_contact_name, primary_contact_email, primary_contact_phone,
            assigned_owner, lifecycle_status, linked_company_id, source_lead_ref,
            annual_contract_value, created_at, updated_at
        ) VALUES (
            #{accountRef}, #{accountName}, #{legalName}, #{billingCountry}, #{billingCurrency},
            #{businessDomain}, #{primaryContactName}, #{primaryContactEmail}, #{primaryContactPhone},
            #{assignedOwner}, #{lifecycleStatus}, #{linkedCompanyId}, #{sourceLeadRef},
            #{annualContractValue}, NOW(), NOW()
        )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertAccount(SaasCommercialAccount account);

    @Update("UPDATE saas_commercial_accounts SET lifecycle_status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateAccountStatus(@Param("id") Long id, @Param("status") String status);

    @Update("""
        UPDATE saas_commercial_accounts
        SET lifecycle_status = 'OFFBOARDED',
            drop_reason = #{reason},
            drop_notes = #{notes},
            drop_date = #{dropDate},
            updated_at = NOW()
        WHERE id = #{id}
    """)
    int offboardCommercialAccount(@Param("id") Long id,
                                  @Param("reason") String reason,
                                  @Param("notes") String notes,
                                  @Param("dropDate") LocalDate dropDate);

    @Update("UPDATE saas_subscriptions SET status = 'CANCELLED', updated_at = NOW() WHERE account_id = #{accountId}")
    int cancelSubscriptionsByAccount(@Param("accountId") Long accountId);

    @Update("UPDATE companies SET is_active = FALSE, updated_at = NOW() WHERE id = #{companyId}")
    int deactivateCompany(@Param("companyId") Long companyId);

    @Select("SELECT linked_company_id FROM saas_commercial_accounts WHERE id = #{id}")
    Long getLinkedCompanyIdByAccountId(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM employees WHERE company_id = #{companyId} AND status = 'ACTIVE'")
    int getTenantActiveSeats(@Param("companyId") Long companyId);

    @Select("SELECT * FROM saas_plans WHERE is_active = TRUE ORDER BY price_per_seat ASC")
    List<SaasPlan> findAllPlans();

    @Insert("""
        INSERT INTO saas_plans (plan_code, plan_name, version, billing_interval, currency, price_per_seat, min_seats, features_summary, is_active, created_at)
        VALUES (#{planCode}, #{planName}, #{version}, #{billingInterval}, #{currency}, #{pricePerSeat}, #{minSeats}, #{featuresSummary}, TRUE, NOW())
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertPlan(SaasPlan plan);

    @Select("""
        SELECT s.id, s.subscription_ref, s.account_id, a.account_name,
               s.plan_id, p.plan_code, p.plan_name, s.status, s.seats_licensed,
               s.period_start, s.period_end, s.next_billing_date, s.auto_renew,
               s.payment_method, s.created_at, s.updated_at
        FROM saas_subscriptions s
        JOIN saas_commercial_accounts a ON s.account_id = a.id
        JOIN saas_plans p ON s.plan_id = p.id
        ORDER BY s.created_at DESC
    """)
    List<SaasSubscription> findAllSubscriptions();

    @Insert("""
        INSERT INTO saas_subscriptions (
            subscription_ref, account_id, plan_id, status, seats_licensed,
            period_start, period_end, next_billing_date, auto_renew, payment_method, created_at, updated_at
        ) VALUES (
            #{subscriptionRef}, #{accountId}, #{planId}, #{status}, #{seatsLicensed},
            #{periodStart}, #{periodEnd}, #{nextBillingDate}, #{autoRenew}, #{paymentMethod}, NOW(), NOW()
        )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertSubscription(SaasSubscription subscription);

    @Select("""
        SELECT c.id, c.contract_ref, c.account_id, a.account_name,
               c.contract_type, c.title, c.amount, c.currency, c.billing_status,
               c.start_date, c.end_date, c.terms_summary, c.invoice_id,
               c.created_at, c.updated_at
        FROM saas_contracts c
        JOIN saas_commercial_accounts a ON c.account_id = a.id
        ORDER BY c.created_at DESC
    """)
    List<SaasContract> findAllContracts();

    @Insert("""
        INSERT INTO saas_contracts (
            contract_ref, account_id, contract_type, title, amount, currency,
            billing_status, start_date, end_date, terms_summary, created_at, updated_at
        ) VALUES (
            #{contractRef}, #{accountId}, #{contractType}, #{title}, #{amount}, #{currency},
            #{billingStatus}, #{startDate}, #{endDate}, #{termsSummary}, NOW(), NOW()
        )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertContract(SaasContract contract);

    @Update("UPDATE saas_contracts SET billing_status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateContractStatus(@Param("id") Long id, @Param("status") String status);

    @Select("""
        SELECT i.id, i.invoice_number, i.account_id, a.account_name,
               i.subscription_id, i.billing_period_start, i.billing_period_end,
               i.subtotal_amount, i.tax_amount, i.total_amount, i.currency,
               i.due_date, i.status, i.paid_at, i.payment_reference, i.created_at
        FROM saas_invoices i
        JOIN saas_commercial_accounts a ON i.account_id = a.id
        ORDER BY i.created_at DESC
    """)
    List<SaasInvoice> findAllInvoices();

    @Insert("""
        INSERT INTO saas_invoices (
            invoice_number, account_id, subscription_id, billing_period_start, billing_period_end,
            subtotal_amount, tax_amount, total_amount, currency, due_date, status, created_at, updated_at
        ) VALUES (
            #{invoiceNumber}, #{accountId}, #{subscriptionId}, #{billingPeriodStart}, #{billingPeriodEnd},
            #{subtotalAmount}, #{taxAmount}, #{totalAmount}, #{currency}, #{dueDate}, #{status}, NOW(), NOW()
        )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertInvoice(SaasInvoice invoice);

    @Update("""
        UPDATE saas_invoices
        SET status = #{status},
            payment_reference = COALESCE(#{paymentReference}, payment_reference),
            paid_at = CASE WHEN #{status} = 'PAID' THEN NOW() ELSE paid_at END,
            updated_at = NOW()
        WHERE id = #{id}
    """)
    int updateInvoiceStatus(@Param("id") Long id,
                            @Param("status") String status,
                            @Param("paymentReference") String paymentReference);

    @Select("""
        SELECT r.id, r.request_ref, r.account_id, a.account_name,
               r.lead_id, r.contact_name, r.contact_email, r.request_type,
               r.title, r.description, r.priority, r.status, r.assigned_to,
               r.target_date, r.resolution_notes, r.created_at, r.updated_at
        FROM saas_customer_requests r
        LEFT JOIN saas_commercial_accounts a ON r.account_id = a.id
        ORDER BY r.created_at DESC
    """)
    List<SaasCustomerRequest> findAllCustomerRequests();

    @Insert("""
        INSERT INTO saas_customer_requests (
            request_ref, account_id, lead_id, contact_name, contact_email, request_type,
            title, description, priority, status, assigned_to, target_date, created_at, updated_at
        ) VALUES (
            #{requestRef}, #{accountId}, #{leadId}, #{contactName}, #{contactEmail}, #{requestType},
            #{title}, #{description}, #{priority}, #{status}, #{assignedTo}, #{targetDate}, NOW(), NOW()
        )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertCustomerRequest(SaasCustomerRequest request);

    @Update("""
        UPDATE saas_customer_requests
        SET status = #{status},
            resolution_notes = COALESCE(#{resolutionNotes}, resolution_notes),
            updated_at = NOW()
        WHERE id = #{id}
    """)
    int updateCustomerRequestStatus(@Param("id") Long id,
                                   @Param("status") String status,
                                   @Param("resolutionNotes") String resolutionNotes);

    @Select("SELECT * FROM lead_inquiries ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<LeadInquiry> findAllLeads(@Param("limit") int limit, @Param("offset") int offset);

    @Update("UPDATE lead_inquiries SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateLeadStatus(@Param("id") Long id, @Param("status") String status);

    @Insert("""
        INSERT INTO saas_audit_events (actor_email, action_type, target_entity, target_id, summary, details_json, ip_address, created_at)
        VALUES (#{actorEmail}, #{actionType}, #{targetEntity}, #{targetId}, #{summary}, #{detailsJson}, #{ipAddress}, NOW())
    """)
    int insertAuditEvent(@Param("actorEmail") String actorEmail,
                         @Param("actionType") String actionType,
                         @Param("targetEntity") String targetEntity,
                         @Param("targetId") String targetId,
                         @Param("summary") String summary,
                         @Param("detailsJson") String detailsJson,
                         @Param("ipAddress") String ipAddress);

    @Select("SELECT * FROM saas_audit_events ORDER BY created_at DESC LIMIT #{limit}")
    List<SaasAuditEvent> findRecentAuditEvents(@Param("limit") int limit);
}
