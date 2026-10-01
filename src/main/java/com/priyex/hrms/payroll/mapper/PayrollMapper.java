package com.priyex.hrms.payroll.mapper;

import com.priyex.hrms.payroll.model.EmployeePayslip;
import com.priyex.hrms.payroll.model.PayrollRun;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PayrollMapper {

    List<PayrollRun> findRunsByCompanyId(@Param("companyId") Long companyId);

    Optional<PayrollRun> findRunById(@Param("id") Long id, @Param("companyId") Long companyId);

    Optional<PayrollRun> findRunByPeriod(@Param("companyId") Long companyId, @Param("month") Integer month, @Param("year") Integer year);

    int insertPayrollRun(PayrollRun run);

    int updatePayrollRun(PayrollRun run);

    List<EmployeePayslip> findPayslipsByRunId(@Param("runId") Long runId);

    List<EmployeePayslip> findPayslipsByEmployeeId(@Param("employeeId") Long employeeId);

    Optional<EmployeePayslip> findPayslipById(@Param("id") Long id);

    Optional<EmployeePayslip> findPayslipByEmployeeAndPeriod(
            @Param("employeeId") Long employeeId,
            @Param("month") Integer month,
            @Param("year") Integer year
    );

    List<EmployeePayslip> findPayslipsFiltered(
            @Param("companyId") Long companyId,
            @Param("employeeId") Long employeeId,
            @Param("year") Integer year,
            @Param("month") Integer month
    );

    int insertPayslip(EmployeePayslip payslip);

    int deletePayslipsByRunId(@Param("runId") Long runId);
}
