package com.priyex.hrms.tax.mapper;

import com.priyex.hrms.tax.model.InvestmentDeclaration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface InvestmentDeclarationMapper {

    Optional<InvestmentDeclaration> findByEmployeeAndYear(
            @Param("employeeId") Long employeeId,
            @Param("financialYear") String financialYear
    );

    Optional<InvestmentDeclaration> findById(@Param("id") Long id);

    List<InvestmentDeclaration> findByCompanyAndYear(
            @Param("companyId") Long companyId,
            @Param("financialYear") String financialYear,
            @Param("status") String status
    );

    int insert(InvestmentDeclaration declaration);

    int update(InvestmentDeclaration declaration);

    int updateStatus(
            @Param("id") Long id,
            @Param("status") String status,
            @Param("hrNotes") String hrNotes,
            @Param("verifiedBy") Long verifiedBy
    );
}
