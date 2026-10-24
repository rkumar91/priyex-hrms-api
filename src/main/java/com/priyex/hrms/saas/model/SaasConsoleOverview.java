package com.priyex.hrms.saas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaasConsoleOverview {
    private Integer totalLeads;
    private Integer newLeads;
    private Integer demosScheduled;
    private Integer activeAccounts;
    private Integer totalSubscribers;
    private BigDecimal estimatedArr;
    private BigDecimal activeArr;
    private BigDecimal activeMrr;
    private Integer openCustomerRequests;
    private Integer overdueFollowups;
    private Integer totalTenants;
    private Integer activeTenants;
    private Integer totalSubscriptions;
    private Integer totalWorkforceEmployees;
    private BigDecimal setupRevenueTotal;
    private BigDecimal amcRevenueTotal;
    private Integer activeContractsCount;
    private BigDecimal totalContractValue;
    private BigDecimal totalCollectedRevenue;
    private BigDecimal overdueReceivables;
}
