package com.priyex.hrms.expense.controller;

import com.priyex.hrms.common.response.ApiResponse;
import com.priyex.hrms.expense.model.ExpenseClaim;
import com.priyex.hrms.security.CurrentUser;
import com.priyex.hrms.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/v1/expenses")
@Tag(name = "Expense Claims Management", description = "Endpoints for submitting, auditing, approving, and reimbursing corporate employee expenses")
public class ExpenseController {

    private final AtomicLong idGenerator = new AtomicLong(100);
    private final List<ExpenseClaim> inMemoryStore = new CopyOnWriteArrayList<>(initDefaultExpenses());

    private static List<ExpenseClaim> initDefaultExpenses() {
        List<ExpenseClaim> list = new ArrayList<>();
        list.add(ExpenseClaim.builder()
                .id(1L)
                .claimNumber("EXP-2026-081")
                .companyId(1L)
                .employeeId(1L)
                .employeeName("Rajesh Kumar")
                .department("Engineering")
                .category("TRAVEL")
                .merchant("Air India / Indigo Express")
                .amount(new BigDecimal("14850.00"))
                .currency("INR")
                .expenseDate(LocalDate.now().minusDays(5))
                .submittedAt(LocalDate.now().minusDays(4).toString())
                .description("Roundtrip flights for on-site client kickoff meeting in Bengaluru.")
                .projectCode("PRJ-FINTECH-01")
                .receiptName("Flight_Tickets_Indigo_BLR.pdf")
                .status("APPROVED")
                .approverRemarks("Approved per executive travel policy band 4.")
                .createdAt(LocalDateTime.now().minusDays(4))
                .updatedAt(LocalDateTime.now().minusDays(3))
                .build());

        list.add(ExpenseClaim.builder()
                .id(2L)
                .claimNumber("EXP-2026-082")
                .companyId(1L)
                .employeeId(2L)
                .employeeName("Priya Sharma")
                .department("Human Resources")
                .category("CLIENT_ENTERTAINMENT")
                .merchant("The Oberoi Grand Hotel")
                .amount(new BigDecimal("6420.00"))
                .currency("INR")
                .expenseDate(LocalDate.now().minusDays(3))
                .submittedAt(LocalDate.now().minusDays(2).toString())
                .description("Executive campus recruitment partner dinner and leadership discussion.")
                .projectCode("HR-TALENT-2026")
                .receiptName("Oberoi_Invoice_RecruitersDinner.pdf")
                .status("PENDING")
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(2))
                .build());

        list.add(ExpenseClaim.builder()
                .id(3L)
                .claimNumber("EXP-2026-083")
                .companyId(1L)
                .employeeId(1L)
                .employeeName("Amit Verma")
                .department("Engineering")
                .category("MOBILE_INTERNET")
                .merchant("Airtel Broadband Fiber")
                .amount(new BigDecimal("1799.00"))
                .currency("INR")
                .expenseDate(LocalDate.now().minusDays(12))
                .submittedAt(LocalDate.now().minusDays(11).toString())
                .description("Monthly remote WFH high-speed fiber internet reimbursement allowance.")
                .projectCode("CORP-WFH-ALLOWANCE")
                .receiptName("Airtel_Bill_Oct2026.pdf")
                .status("REIMBURSED")
                .approverRemarks("Processed in September payroll cycle.")
                .reimbursedDate(LocalDate.now().minusDays(8))
                .reimbursementRef("NEFT-88902143")
                .createdAt(LocalDateTime.now().minusDays(11))
                .updatedAt(LocalDateTime.now().minusDays(8))
                .build());

        list.add(ExpenseClaim.builder()
                .id(4L)
                .claimNumber("EXP-2026-084")
                .companyId(1L)
                .employeeId(3L)
                .employeeName("Sneha Patel")
                .department("Marketing")
                .category("TRAINING")
                .merchant("AWS Cloud Certification Portal")
                .amount(new BigDecimal("12500.00"))
                .currency("INR")
                .expenseDate(LocalDate.now().minusDays(8))
                .submittedAt(LocalDate.now().minusDays(7).toString())
                .description("AWS Certified Solutions Architect Exam registration fees.")
                .projectCode("DEV-UPSKILL-Q4")
                .receiptName("AWS_Exam_Receipt_12500.pdf")
                .status("APPROVED")
                .approverRemarks("Approved under annual workforce training budget.")
                .createdAt(LocalDateTime.now().minusDays(7))
                .updatedAt(LocalDateTime.now().minusDays(6))
                .build());

        return list;
    }

    @GetMapping
    @Operation(summary = "Get all expense claims for current company")
    public ResponseEntity<ApiResponse<List<ExpenseClaim>>> listExpenses(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "category", required = false) String category
    ) {
        List<ExpenseClaim> list = new ArrayList<>(inMemoryStore);
        if (status != null && !status.equalsIgnoreCase("ALL")) {
            list.removeIf(c -> !c.getStatus().equalsIgnoreCase(status));
        }
        if (category != null && !category.equalsIgnoreCase("ALL")) {
            list.removeIf(c -> !c.getCategory().equalsIgnoreCase(category));
        }
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping
    @Operation(summary = "Submit a new corporate expense claim")
    public ResponseEntity<ApiResponse<ExpenseClaim>> submitExpense(
            @CurrentUser UserPrincipal currentUser,
            @RequestBody ExpenseClaim request
    ) {
        long id = idGenerator.incrementAndGet();
        request.setId(id);
        request.setClaimNumber("EXP-2026-" + String.format("%03d", id));
        request.setCompanyId(currentUser != null && currentUser.getCompanyId() != null ? currentUser.getCompanyId() : 1L);
        if (request.getEmployeeName() == null && currentUser != null) {
            request.setEmployeeName(currentUser.getUsername());
        }
        if (request.getCurrency() == null) {
            request.setCurrency("INR");
        }
        request.setStatus("PENDING");
        request.setSubmittedAt(LocalDate.now().toString());
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());

        inMemoryStore.add(0, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(request, "Expense claim filed successfully"));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve expense claim for payout")
    public ResponseEntity<ApiResponse<ExpenseClaim>> approveExpense(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @RequestParam(name = "comment", required = false) String comment
    ) {
        for (ExpenseClaim c : inMemoryStore) {
            if (c.getId().equals(id)) {
                c.setStatus("APPROVED");
                c.setApproverRemarks(comment != null ? comment : "Approved by Manager");
                c.setUpdatedAt(LocalDateTime.now());
                return ResponseEntity.ok(ApiResponse.success(c, "Claim approved successfully"));
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject expense claim")
    public ResponseEntity<ApiResponse<ExpenseClaim>> rejectExpense(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @RequestParam(name = "comment", required = false) String comment
    ) {
        for (ExpenseClaim c : inMemoryStore) {
            if (c.getId().equals(id)) {
                c.setStatus("REJECTED");
                c.setApproverRemarks(comment != null ? comment : "Non-compliant with expense policy limits");
                c.setUpdatedAt(LocalDateTime.now());
                return ResponseEntity.ok(ApiResponse.success(c, "Claim rejected"));
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/reimburse")
    @Operation(summary = "Mark expense claim as reimbursed via payroll or bank transfer")
    public ResponseEntity<ApiResponse<ExpenseClaim>> reimburseExpense(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @RequestParam(name = "ref", required = false) String ref
    ) {
        for (ExpenseClaim c : inMemoryStore) {
            if (c.getId().equals(id)) {
                c.setStatus("REIMBURSED");
                c.setReimbursedDate(LocalDate.now());
                c.setReimbursementRef(ref != null ? ref : "NEFT-" + System.currentTimeMillis());
                c.setUpdatedAt(LocalDateTime.now());
                return ResponseEntity.ok(ApiResponse.success(c, "Claim marked as reimbursed"));
            }
        }
        return ResponseEntity.notFound().build();
    }
}
