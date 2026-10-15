package com.priyex.hrms.expense.mapper;

import com.priyex.hrms.expense.model.ExpenseClaim;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ExpenseMapper {

    List<ExpenseClaim> findAll(
            @Param("companyId") Long companyId,
            @Param("status") String status,
            @Param("category") String category
    );

    List<ExpenseClaim> findByManagerEmployeeId(
            @Param("managerEmployeeId") Long managerEmployeeId,
            @Param("companyId") Long companyId
    );

    List<ExpenseClaim> findByEmployeeId(
            @Param("employeeId") Long employeeId,
            @Param("companyId") Long companyId
    );

    Optional<ExpenseClaim> findById(
            @Param("id") Long id,
            @Param("companyId") Long companyId
    );

    int insert(ExpenseClaim claim);

    int updateManagerDecision(
            @Param("id") Long id,
            @Param("companyId") Long companyId,
            @Param("status") String status,
            @Param("comment") String comment,
            @Param("managerId") Long managerId
    );

    int updateHrDecision(
            @Param("id") Long id,
            @Param("companyId") Long companyId,
            @Param("status") String status,
            @Param("comment") String comment,
            @Param("hrUserId") Long hrUserId
    );

    int updateReimbursed(
            @Param("id") Long id,
            @Param("companyId") Long companyId,
            @Param("ref") String ref
    );
}
