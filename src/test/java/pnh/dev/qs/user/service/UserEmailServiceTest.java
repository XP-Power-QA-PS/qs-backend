package pnh.dev.qs.user.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.IContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import pnh.dev.qs.user.dto.EmailRecipientDTO;
import pnh.dev.qs.user.dto.EmailSendResultDTO;
import pnh.dev.qs.user.dto.MeetingEmailRequest;
import pnh.dev.qs.user.service.impl.UserEmailServiceImpl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private IcsCalendarService icsCalendarService;

    private UserEmailServiceImpl userEmailService;

    @BeforeEach
    void setUp() {
        userEmailService = new UserEmailServiceImpl(mailSender, templateEngine, icsCalendarService);
        ReflectionTestUtils.setField(userEmailService, "fromEmail", "noreply@qs-system.com");
        ReflectionTestUtils.setField(userEmailService, "fromName", "QS Quality System");
        ReflectionTestUtils.setField(userEmailService, "mailEnabled", true);
    }

    @Test
    void testSendMeetingInvitation_SyncMethodReturnsImmediately() {
        MeetingEmailRequest request = MeetingEmailRequest.builder()
                .trackingNo("2026-09-0002")
                .meetingDate(LocalDate.of(2026, 9, 16))
                .startTime(LocalTime.of(10, 0))
                .roomLocation("Room 101")
                .agenda("Review defect report")
                .recipients(List.of(
                        EmailRecipientDTO.builder().email("to1@example.com").recipientType("TO").build(),
                        EmailRecipientDTO.builder().email("to2@example.com").recipientType("TO").build(),
                        EmailRecipientDTO.builder().email("cc1@example.com").recipientType("CC").build()
                ))
                .build();

        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("mail/meeting-invitation"), any(IContext.class))).thenReturn("<html>Email Content</html>");
        when(icsCalendarService.generateMeetingIcs(any(), anyString(), anyString())).thenReturn("ICS_CONTENT".getBytes());

        EmailSendResultDTO result = userEmailService.sendMeetingInvitation(request);

        assertNotNull(result);
        assertEquals("2026-09-0002", result.getTrackingNo());
        assertEquals(3, result.getTotalRecipients());
        assertEquals(2, result.getToCount());
        assertEquals(1, result.getCcCount());
        assertTrue(result.isAsync());
    }
}
