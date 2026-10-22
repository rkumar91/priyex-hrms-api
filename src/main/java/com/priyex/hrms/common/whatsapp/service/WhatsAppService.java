package com.priyex.hrms.common.whatsapp.service;

public interface WhatsAppService {

    /**
     * Dispatches an interactive WhatsApp demo booking confirmation to the prospective client.
     */
    boolean sendDemoBookingConfirmation(String phoneNumber,
                                        String recipientName,
                                        String companyName,
                                        String inquiryRef,
                                        String scheduledWindow,
                                        String assignedConsultant);

    /**
     * (Optional) Dispatches an automated internal alert to the enterprise solutions / sales team.
     */
    boolean sendInternalSalesAlert(String recipientPhone,
                                   String companyName,
                                   String contactPerson,
                                   String email,
                                   String phone,
                                   String employeeCount,
                                   String interestModule,
                                   String scheduledWindow,
                                   String inquiryRef);
}
