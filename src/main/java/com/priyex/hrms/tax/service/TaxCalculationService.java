package com.priyex.hrms.tax.service;

import com.priyex.hrms.tax.dto.*;
import com.priyex.hrms.tax.model.InvestmentDeclaration;

import java.util.List;

public interface TaxCalculationService {

    InvestmentDeclaration getDeclaration(Long employeeId, String financialYear);

    InvestmentDeclaration saveOrSubmitDeclaration(Long companyId, Long employeeId, SubmitDeclarationRequest request);

    TaxCalculationResponse calculateTax(Long companyId, Long employeeId, String financialYear, String regimeOverride);

    Form16Response generateForm16(Long companyId, Long employeeId, String financialYear);

    Form12bbResponse generateForm12bb(Long companyId, Long employeeId, String financialYear);

    List<InvestmentDeclaration> getDeclarationsForCompany(Long companyId, String financialYear, String status);

    InvestmentDeclaration verifyDeclaration(Long companyId, Long id, Long actorId, VerifyDeclarationRequest request);
}
