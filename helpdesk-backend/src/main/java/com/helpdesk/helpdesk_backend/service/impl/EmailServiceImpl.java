package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    @Async
    public void sendTicketCreatedEmail(String toEmail, String recipientName,
                                        String ticketNumber, String subject) {
        String emailSubject = "Ticket Created: " + ticketNumber;
        String body = buildTicketCreatedBody(recipientName, ticketNumber, subject);
        sendHtmlEmail(toEmail, emailSubject, body);
    }

    @Override
    @Async
    public void sendTicketAssignedEmail(String toEmail, String recipientName,
                                         String ticketNumber, String subject,
                                         String assignedByName) {
        String emailSubject = "Ticket Assigned to You: " + ticketNumber;
        String body = buildTicketAssignedBody(
                recipientName, ticketNumber, subject, assignedByName);
        sendHtmlEmail(toEmail, emailSubject, body);
    }

    @Override
    @Async
    public void sendStatusChangedEmail(String toEmail, String recipientName,
                                        String ticketNumber, String subject,
                                        String oldStatus, String newStatus) {
        String emailSubject = "Ticket Status Updated: " + ticketNumber;
        String body = buildStatusChangedBody(
                recipientName, ticketNumber, subject, oldStatus, newStatus);
        sendHtmlEmail(toEmail, emailSubject, body);
    }

    @Override
    @Async
    public void sendCommentAddedEmail(String toEmail, String recipientName,
                                       String ticketNumber, String subject,
                                       String commenterName, String comment) {
        String emailSubject = "New Comment on Ticket: " + ticketNumber;
        String body = buildCommentAddedBody(
                recipientName, ticketNumber, subject, commenterName, comment);
        sendHtmlEmail(toEmail, emailSubject, body);
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}. Error: {}", toEmail, e.getMessage());
        }
    }

    private String buildTicketCreatedBody(String name, String ticketNumber,
                                           String subject) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px;'>"
                + "<h2 style='color: #2f6fed;'>Help Desk — Ticket Created</h2>"
                + "<p>Hello <strong>" + name + "</strong>,</p>"
                + "<p>Your ticket has been successfully created.</p>"
                + "<div style='background: #f4f6f8; padding: 16px; "
                + "border-radius: 8px; margin: 16px 0;'>"
                + "<p><strong>Ticket Number:</strong> " + ticketNumber + "</p>"
                + "<p><strong>Subject:</strong> " + subject + "</p>"
                + "<p><strong>Status:</strong> OPEN</p>"
                + "</div>"
                + "<p>Our support team will review your ticket shortly.</p>"
                + "<p style='color: #888;'>Help Desk System</p>"
                + "</div>";
    }

    private String buildTicketAssignedBody(String name, String ticketNumber,
                                            String subject, String assignedByName) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px;'>"
                + "<h2 style='color: #2f6fed;'>Help Desk — Ticket Assigned</h2>"
                + "<p>Hello <strong>" + name + "</strong>,</p>"
                + "<p>A ticket has been assigned to you by "
                + "<strong>" + assignedByName + "</strong>.</p>"
                + "<div style='background: #f4f6f8; padding: 16px; "
                + "border-radius: 8px; margin: 16px 0;'>"
                + "<p><strong>Ticket Number:</strong> " + ticketNumber + "</p>"
                + "<p><strong>Subject:</strong> " + subject + "</p>"
                + "<p><strong>Status:</strong> ASSIGNED</p>"
                + "</div>"
                + "<p>Please review and start working on this ticket.</p>"
                + "<p style='color: #888;'>Help Desk System</p>"
                + "</div>";
    }

    private String buildStatusChangedBody(String name, String ticketNumber,
                                           String subject, String oldStatus,
                                           String newStatus) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px;'>"
                + "<h2 style='color: #2f6fed;'>Help Desk — Ticket Status Updated</h2>"
                + "<p>Hello <strong>" + name + "</strong>,</p>"
                + "<p>The status of your ticket has been updated.</p>"
                + "<div style='background: #f4f6f8; padding: 16px; "
                + "border-radius: 8px; margin: 16px 0;'>"
                + "<p><strong>Ticket Number:</strong> " + ticketNumber + "</p>"
                + "<p><strong>Subject:</strong> " + subject + "</p>"
                + "<p><strong>Previous Status:</strong> " + oldStatus + "</p>"
                + "<p><strong>New Status:</strong> " + newStatus + "</p>"
                + "</div>"
                + "<p style='color: #888;'>Help Desk System</p>"
                + "</div>";
    }

    private String buildCommentAddedBody(String name, String ticketNumber,
                                          String subject, String commenterName,
                                          String comment) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px;'>"
                + "<h2 style='color: #2f6fed;'>Help Desk — New Comment</h2>"
                + "<p>Hello <strong>" + name + "</strong>,</p>"
                + "<p><strong>" + commenterName
                + "</strong> added a comment on your ticket.</p>"
                + "<div style='background: #f4f6f8; padding: 16px; "
                + "border-radius: 8px; margin: 16px 0;'>"
                + "<p><strong>Ticket Number:</strong> " + ticketNumber + "</p>"
                + "<p><strong>Subject:</strong> " + subject + "</p>"
                + "<p><strong>Comment:</strong> " + comment + "</p>"
                + "</div>"
                + "<p style='color: #888;'>Help Desk System</p>"
                + "</div>";
    }
}