package pnh.dev.qs.user.service.impl;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import pnh.dev.qs.user.dto.EmailRecipientDTO;
import pnh.dev.qs.user.dto.EmailSendResultDTO;
import pnh.dev.qs.user.dto.MeetingEmailRequest;
import pnh.dev.qs.user.service.IcsCalendarService;
import pnh.dev.qs.user.service.UserEmailService;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEmailServiceImpl implements UserEmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final IcsCalendarService icsCalendarService;

    @Value("${app.mail.from-email:noreply.qs.system@gmail.com}")
    private String fromEmail;

    @Value("${app.mail.from-name:QS Quality System}")
    private String fromName;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public EmailSendResultDTO sendMeetingInvitation(MeetingEmailRequest request) {
        log.info("Processing meeting invitation email request for complaint trackingNo: {}", request.getTrackingNo());

        if (!mailEnabled) {
            log.warn("Mail sending is disabled (app.mail.enabled=false).");
            throw new pnh.dev.qs.exception.custom.BadRequestException(
                    "Tính năng gửi mail đang bị tắt (app.mail.enabled=false trong application-dev.properties). Hãy đổi app.mail.enabled=true để gửi thư thật!");
        }

        int toCount = 0;
        int ccCount = 0;
        if (request.getRecipients() != null) {
            for (EmailRecipientDTO r : request.getRecipients()) {
                if ("CC".equalsIgnoreCase(r.getRecipientType())) {
                    ccCount++;
                } else {
                    toCount++;
                }
            }
        }

        int total = toCount + ccCount;

        // Trigger asynchronous email dispatch
        sendMeetingInvitationAsync(request);

        return EmailSendResultDTO.builder()
                .trackingNo(request.getTrackingNo())
                .totalRecipients(total)
                .toCount(toCount)
                .ccCount(ccCount)
                .message("Đã kích hoạt gửi thư mời họp đồng loạt đến " + total + " thành viên.")
                .async(true)
                .timestamp(Instant.now())
                .build();
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendMeetingInvitationAsync(MeetingEmailRequest request) {
        if (!mailEnabled) {
            log.warn("Mail sending is disabled (app.mail.enabled=false). Skipping email for trackingNo: {}",
                    request.getTrackingNo());
            return;
        }

        try {
            log.info("Starting async mail sending for trackingNo: {}", request.getTrackingNo());

            List<String> toList = new ArrayList<>();
            List<String> ccList = new ArrayList<>();

            if (request.getRecipients() != null) {
                for (EmailRecipientDTO recipient : request.getRecipients()) {
                    if (recipient.getEmail() == null || recipient.getEmail().isBlank()) {
                        continue;
                    }
                    String email = recipient.getEmail().trim();
                    if ("CC".equalsIgnoreCase(recipient.getRecipientType())) {
                        ccList.add(email);
                    } else {
                        toList.add(email);
                    }
                }
            }

            if (toList.isEmpty()) {
                log.warn("No TO recipient specified for trackingNo: {}. Aborting email send.", request.getTrackingNo());
                return;
            }

            // 1. Resolve organizer info
            String orgEmail = (request.getOrganizerEmail() != null && !request.getOrganizerEmail().isBlank())
                    ? request.getOrganizerEmail().trim()
                    : fromEmail;
            String orgName = (request.getOrganizerName() != null && !request.getOrganizerName().isBlank())
                    ? request.getOrganizerName().trim()
                    : fromName;

            // 2. Prepare Thymeleaf Context
            Context context = new Context();
            context.setVariable("trackingNo", request.getTrackingNo());
            context.setVariable("model", request.getModel());
            context.setVariable("customerName", request.getCustomerName());
            context.setVariable("issueDescription", request.getIssueDescription());
            context.setVariable("meetingDate", request.getMeetingDate().format(DATE_FORMATTER));
            context.setVariable("startTime", request.getStartTime().format(TIME_FORMATTER));
            context.setVariable("endTime", request.getEndTime() != null ? request.getEndTime().format(TIME_FORMATTER) : null);
            context.setVariable("roomLocation", request.getRoomLocation());
            context.setVariable("agenda", request.getAgenda());
            context.setVariable("organizerName", orgName);
            context.setVariable("organizerEmail", orgEmail);
            context.setVariable("recipients", request.getRecipients());

            // 3. Render HTML body
            String htmlContent = templateEngine.process("mail/meeting-invitation", context);

            // 4. Generate .ics calendar invite with organizer info
            byte[] icsBytes = icsCalendarService.generateMeetingIcs(request, orgEmail, orgName);

            // 5. Build MimeMessage
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            if (request.getOrganizerEmail() != null && !request.getOrganizerEmail().isBlank()) {
                helper.setReplyTo(request.getOrganizerEmail().trim(), orgName);
            }
            helper.setTo(toList.toArray(new String[0]));
            if (!ccList.isEmpty()) {
                helper.setCc(ccList.toArray(new String[0]));
            }

            String subject = "[QS-ALERT] Thư mời họp đánh giá sơ bộ: Khiếu nại #" + request.getTrackingNo()
                    + (request.getModel() != null ? " - " + request.getModel() : "");
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            // Attach .ics file
            if (icsBytes != null && icsBytes.length > 0) {
                helper.addAttachment("meeting-invite.ics",
                        new ByteArrayResource(icsBytes),
                        "text/calendar; charset=UTF-8; method=REQUEST");
            }

            mailSender.send(message);
            log.info("Successfully sent meeting invitation email for complaint {} to {} recipients (TO: {}, CC: {})",
                    request.getTrackingNo(), toList.size() + ccList.size(), toList.size(), ccList.size());

        } catch (Exception e) {
            log.error("Failed to send meeting invitation email for trackingNo {}: {}", request.getTrackingNo(), e.getMessage(), e);
        }
    }

    @Override
    public void sendHtmlEmail(String[] to, String[] cc, String subject, String htmlBody, byte[] attachmentData, String attachmentFilename, String contentType) {
        if (!mailEnabled) {
            log.warn("Mail sending is disabled (app.mail.enabled=false).");
            throw new pnh.dev.qs.exception.custom.BadRequestException(
                    "Tính năng gửi mail đang bị tắt (app.mail.enabled=false trong application-dev.properties). Hãy đổi app.mail.enabled=true để gửi thư thật!");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            if (cc != null && cc.length > 0) {
                helper.setCc(cc);
            }
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            if (attachmentData != null && attachmentFilename != null) {
                helper.addAttachment(attachmentFilename, new ByteArrayResource(attachmentData), contentType != null ? contentType : "application/octet-stream");
            }

            mailSender.send(message);
            log.info("HTML email successfully sent to: {}", (Object) to);
        } catch (org.springframework.mail.MailAuthenticationException e) {
            log.error("SMTP Authentication failed: {}", e.getMessage());
            throw new pnh.dev.qs.exception.custom.BadRequestException(
                    "Xác thực máy chủ SMTP thất bại! Chưa cấu hình hoặc sai Mật khẩu ứng dụng (App Password 16 ký tự) của Gmail trong application-dev.properties.");
        } catch (Exception e) {
            log.error("Failed to send HTML email: {}", e.getMessage(), e);
            throw new RuntimeException("Email sending failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        if (!mailEnabled) {
            log.warn("Mail sending is disabled. Skipping password reset email for: {}", toEmail);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("[QS System] Yêu cầu đặt lại mật khẩu");

            String content = "<div style='font-family: sans-serif; padding: 20px;'>"
                    + "<h2>Yêu cầu đặt lại mật khẩu</h2>"
                    + "<p>Bạn đã gửi yêu cầu đặt lại mật khẩu cho tài khoản tại QS System.</p>"
                    + "<p>Mã xác thực của bạn là: <strong style='font-size: 18px; color: #006194;'>" + resetToken + "</strong></p>"
                    + "<p>Mã này có hiệu lực trong vòng 30 phút. Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email.</p>"
                    + "</div>";

            helper.setText(content, true);
            mailSender.send(message);
            log.info("Password reset email successfully sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage(), e);
        }
    }
}
