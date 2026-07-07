package com.helpdesk.helpdesk_backend.service;

public interface EmailService {

    void sendTicketCreatedEmail(String toEmail, String recipientName,
                                 String ticketNumber, String subject);

    void sendTicketAssignedEmail(String toEmail, String recipientName,
                                  String ticketNumber, String subject,
                                  String assignedByName);

    void sendStatusChangedEmail(String toEmail, String recipientName,
                                 String ticketNumber, String subject,
                                 String oldStatus, String newStatus);

    void sendCommentAddedEmail(String toEmail, String recipientName,
                                String ticketNumber, String subject,
                                String commenterName, String comment);
}