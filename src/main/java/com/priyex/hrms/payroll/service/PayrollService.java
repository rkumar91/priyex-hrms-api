package com.priyex.hrms.payroll.service;

import com.priyex.hrms.payroll.dto.CtcBreakdownResponse;
import com.priyex.hrms.payroll.dto.ExecutePayrollRequest;
import com.priyex.hrms.payroll.dto.PayrollSummaryResponse;
import com.priyex.hrms.payroll.model.EmployeePayslip;
import com.priyex.hrms.payroll.model.PayrollRun;

import java.util.List;

public interface PayrollService {

    PayrollSummaryResponse getPayrollSummary(Long companyId);

    PayrollRun executePayrollRun(Long companyId, Long actorUserId, ExecutePayrollRequest request);

    List<PayrollRun> getPayrollRuns(Long companyId);

    PayrollRun getPayrollRun(Long companyId, Long runId);

    List<EmployeePayslip> getPayslipsForRun(Long companyId, Long runId);

    CtcBreakdownResponse getMyCtc(Long companyId, Long employeeId);

    List<EmployeePayslip> getMyPayslips(Long employeeId);

    EmployeePayslip getPayslip(Long payslipId);
}
