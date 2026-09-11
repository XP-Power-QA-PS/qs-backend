package pnh.dev.qs.user.service.impl;

import org.springframework.stereotype.Service;
import pnh.dev.qs.user.dto.EmailRecipientDTO;
import pnh.dev.qs.user.dto.MeetingEmailRequest;
import pnh.dev.qs.user.service.IcsCalendarService;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class IcsCalendarServiceImpl implements IcsCalendarService {

    private static final DateTimeFormatter ICS_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneId.of("UTC"));

    @Override
    public byte[] generateMeetingIcs(MeetingEmailRequest request, String organizerEmail, String organizerName) {
        StringBuilder sb = new StringBuilder();
        String crlf = "\r\n";

        // Calculate start and end UTC timestamps
        ZoneId localZone = ZoneId.systemDefault();
        LocalDateTime startLocal = LocalDateTime.of(request.getMeetingDate(), request.getStartTime());
        ZonedDateTime startUtc = startLocal.atZone(localZone).withZoneSameInstant(ZoneId.of("UTC"));

        LocalDateTime endLocal = request.getEndTime() != null
                ? LocalDateTime.of(request.getMeetingDate(), request.getEndTime())
                : startLocal.plusHours(1);
        ZonedDateTime endUtc = endLocal.atZone(localZone).withZoneSameInstant(ZoneId.of("UTC"));

        String dtStamp = ICS_DATE_TIME_FORMATTER.format(Instant.now());
        String dtStart = ICS_DATE_TIME_FORMATTER.format(startUtc);
        String dtEnd = ICS_DATE_TIME_FORMATTER.format(endUtc);

        String uid = UUID.randomUUID() + "@qs-system";
        String summary = "[Meeting] Preliminary Review: Complaint #" + escapeIcsText(request.getTrackingNo())
                + (request.getModel() != null ? " - " + escapeIcsText(request.getModel()) : "");
        String location = escapeIcsText(request.getRoomLocation());

        StringBuilder description = new StringBuilder();
        description.append("Customer Complaint Preliminary Review Meeting").append("\\n");
        description.append("Tracking No: ").append(request.getTrackingNo()).append("\\n");
        if (request.getModel() != null) {
            description.append("Model: ").append(request.getModel()).append("\\n");
        }
        if (request.getCustomerName() != null) {
            description.append("Customer: ").append(request.getCustomerName()).append("\\n");
        }
        if (request.getIssueDescription() != null) {
            description.append("Issue: ").append(request.getIssueDescription()).append("\\n");
        }
        description.append("\\nAgenda: ").append(request.getAgenda());

        sb.append("BEGIN:VCALENDAR").append(crlf);
        sb.append("VERSION:2.0").append(crlf);
        sb.append("PRODID:-//QS System//Customer Complaint Meeting//EN").append(crlf);
        sb.append("CALSCALE:GREGORIAN").append(crlf);
        sb.append("METHOD:REQUEST").append(crlf);

        sb.append("BEGIN:VEVENT").append(crlf);
        sb.append("UID:").append(uid).append(crlf);
        sb.append("DTSTAMP:").append(dtStamp).append(crlf);
        sb.append("DTSTART:").append(dtStart).append(crlf);
        sb.append("DTEND:").append(dtEnd).append(crlf);
        sb.append("SUMMARY:").append(summary).append(crlf);
        sb.append("DESCRIPTION:").append(escapeIcsText(description.toString())).append(crlf);
        sb.append("LOCATION:").append(location).append(crlf);
        sb.append("STATUS:CONFIRMED").append(crlf);
        sb.append("SEQUENCE:0").append(crlf);

        // Organizer
        String orgName = (organizerName != null && !organizerName.isBlank()) ? organizerName : "QS System";
        sb.append("ORGANIZER;CN=").append(escapeIcsText(orgName))
                .append(":mailto:").append(organizerEmail).append(crlf);

        // Attendees
        if (request.getRecipients() != null) {
            for (EmailRecipientDTO recipient : request.getRecipients()) {
                String rName = recipient.getName() != null ? recipient.getName() : recipient.getEmail();
                String role = "CC".equalsIgnoreCase(recipient.getRecipientType()) ? "OPT-PARTICIPANT" : "REQ-PARTICIPANT";
                sb.append("ATTENDEE;ROLE=").append(role)
                        .append(";PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=")
                        .append(escapeIcsText(rName))
                        .append(":mailto:").append(recipient.getEmail().trim())
                        .append(crlf);
            }
        }

        sb.append("END:VEVENT").append(crlf);
        sb.append("END:VCALENDAR").append(crlf);

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeIcsText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n");
    }
}
