package com.priyex.hrms.lead.dto;

import java.util.List;

public class PlatformStatsResponse {

    private int activeClients;
    private int totalEmployeesManaged;
    private String uptimeSla;
    private String payrollCalculationAccuracy;
    private String averagePayrollRunTime;
    private List<String> supportedModules;
    private List<String> complianceStandards;

    public PlatformStatsResponse() {
    }

    public PlatformStatsResponse(int activeClients, int totalEmployeesManaged, String uptimeSla, String payrollCalculationAccuracy, String averagePayrollRunTime, List<String> supportedModules, List<String> complianceStandards) {
        this.activeClients = activeClients;
        this.totalEmployeesManaged = totalEmployeesManaged;
        this.uptimeSla = uptimeSla;
        this.payrollCalculationAccuracy = payrollCalculationAccuracy;
        this.averagePayrollRunTime = averagePayrollRunTime;
        this.supportedModules = supportedModules;
        this.complianceStandards = complianceStandards;
    }

    public int getActiveClients() {
        return activeClients;
    }

    public void setActiveClients(int activeClients) {
        this.activeClients = activeClients;
    }

    public int getTotalEmployeesManaged() {
        return totalEmployeesManaged;
    }

    public void setTotalEmployeesManaged(int totalEmployeesManaged) {
        this.totalEmployeesManaged = totalEmployeesManaged;
    }

    public String getUptimeSla() {
        return uptimeSla;
    }

    public void setUptimeSla(String uptimeSla) {
        this.uptimeSla = uptimeSla;
    }

    public String getPayrollCalculationAccuracy() {
        return payrollCalculationAccuracy;
    }

    public void setPayrollCalculationAccuracy(String payrollCalculationAccuracy) {
        this.payrollCalculationAccuracy = payrollCalculationAccuracy;
    }

    public String getAveragePayrollRunTime() {
        return averagePayrollRunTime;
    }

    public void setAveragePayrollRunTime(String averagePayrollRunTime) {
        this.averagePayrollRunTime = averagePayrollRunTime;
    }

    public List<String> getSupportedModules() {
        return supportedModules;
    }

    public void setSupportedModules(List<String> supportedModules) {
        this.supportedModules = supportedModules;
    }

    public List<String> getComplianceStandards() {
        return complianceStandards;
    }

    public void setComplianceStandards(List<String> complianceStandards) {
        this.complianceStandards = complianceStandards;
    }
}
