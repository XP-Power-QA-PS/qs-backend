package pnh.dev.qs.user.service;

import pnh.dev.qs.user.dto.EmailSendResultDTO;
import pnh.dev.qs.user.dto.MeetingEmailRequest;

public interface UserEmailService {

    EmailSendResultDTO sendMeetingInvitation(MeetingEmailRequest request);

    void sendMeetingInvitationAsync(MeetingEmailRequest request);

    void sendHtmlEmail(String[] to, String[] cc, String subject, String htmlBody, byte[] attachmentData, String attachmentFilename, String contentType);

    void sendPasswordResetEmail(String toEmail, String resetToken);
}
