package pnh.dev.qs.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pnh.dev.qs.user.dto.EmailRecipientDTO;
import pnh.dev.qs.user.dto.MeetingEmailRequest;
import pnh.dev.qs.user.service.impl.IcsCalendarServiceImpl;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IcsCalendarServiceTest {

    private IcsCalendarService icsCalendarService;

    @BeforeEach
    void setUp() {
        icsCalendarService = new IcsCalendarServiceImpl();
    }

    @Test
    void testGenerateMeetingIcs_Success() {
        MeetingEmailRequest request = MeetingEmailRequest.builder()
                .trackingNo("2026-09-0001")
                .model("Model P900")
                .customerName("Acme Corp")
                .issueDescription("Voltage drop at pin 4")
                .meetingDate(LocalDate.of(2026, 9, 15))
                .startTime(LocalTime.of(9, 30))
                .endTime(LocalTime.of(10, 30))
                .roomLocation("Room 201 / Teams")
                .agenda("Preliminary review and containment plan")
                .recipients(List.of(
                        EmailRecipientDTO.builder()
                                .name("Nguyen Van A")
                                .email("a.nguyen@example.com")
                                .department("PE")
                                .recipientType("TO")
                                .build(),
                        EmailRecipientDTO.builder()
                                .name("Tran Van B")
                                .email("b.tran@example.com")
                                .department("QA")
                                .recipientType("CC")
                                .build()
                ))
                .build();

        byte[] icsBytes = icsCalendarService.generateMeetingIcs(
                request,
                "organizer@example.com",
                "CQE Quality Team"
        );

        assertNotNull(icsBytes);
        String icsContent = new String(icsBytes, StandardCharsets.UTF_8);

        assertTrue(icsContent.contains("BEGIN:VCALENDAR"));
        assertTrue(icsContent.contains("END:VCALENDAR"));
        assertTrue(icsContent.contains("BEGIN:VEVENT"));
        assertTrue(icsContent.contains("END:VEVENT"));

        assertTrue(icsContent.contains("2026-09-0001"));
        assertTrue(icsContent.contains("Room 201 / Teams"));
        assertTrue(icsContent.contains("ORGANIZER;CN=CQE Quality Team:mailto:organizer@example.com"));

        // Check Attendees (TO = REQ-PARTICIPANT, CC = OPT-PARTICIPANT)
        assertTrue(icsContent.contains("ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=Nguyen Van A:mailto:a.nguyen@example.com"));
        assertTrue(icsContent.contains("ATTENDEE;ROLE=OPT-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=Tran Van B:mailto:b.tran@example.com"));
    }
}
