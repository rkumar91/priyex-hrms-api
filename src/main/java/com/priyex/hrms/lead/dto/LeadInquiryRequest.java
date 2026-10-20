package com.priyex.hrms.lead.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LeadInquiryRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 120, message = "Name must not exceed 120 characters")
    private String fullName;

    @NotBlank(message = "Work email is required")
    @Email(message = "Must be a valid email address")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String workEmail;

    @NotBlank(message = "Company name is required")
    @Size(max = 150, message = "Company name must not exceed 150 characters")
    private String companyName;

    private String phoneNumber;

    private String employeeCount; // e.g. "1-50", "51-250", "251-1000", "1000+"

    private String interestModule; // e.g. "Full Suite", "Core HR", "Payroll & Tax", "Time & Attendance", "Expenses", "Multi-Tenant Cloud"

    private String inquiryType; // "DEMO_REQUEST", "SALES_CONSULTATION", "PRICING_INQUIRY"

    private String preferredDate;

    private String preferredTimeSlot;

    private String message;

    public LeadInquiryRequest() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getWorkEmail() {
        return workEmail;
    }

    public void setWorkEmail(String workEmail) {
        this.workEmail = workEmail;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(String employeeCount) {
        this.employeeCount = employeeCount;
    }

    public String getInterestModule() {
        return interestModule;
    }

    public void setInterestModule(String interestModule) {
        this.interestModule = interestModule;
    }

    public String getInquiryType() {
        return inquiryType;
    }

    public void setInquiryType(String inquiryType) {
        this.inquiryType = inquiryType;
    }

    public String getPreferredDate() {
        return preferredDate;
    }

    public void setPreferredDate(String preferredDate) {
        this.preferredDate = preferredDate;
    }

    public String getPreferredTimeSlot() {
        return preferredTimeSlot;
    }

    public void setPreferredTimeSlot(String preferredTimeSlot) {
        this.preferredTimeSlot = preferredTimeSlot;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
