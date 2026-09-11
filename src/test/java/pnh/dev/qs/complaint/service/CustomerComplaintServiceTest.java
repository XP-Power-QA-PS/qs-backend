package pnh.dev.qs.complaint.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import pnh.dev.qs.complaint.dto.ComplaintCreateRequest;
import pnh.dev.qs.complaint.dto.ComplaintMeetingResponse;
import pnh.dev.qs.complaint.dto.ComplaintResponse;
import pnh.dev.qs.complaint.dto.ComplaintScheduleMeetingRequest;
import pnh.dev.qs.complaint.dto.MeetingConcludeRequest;
import pnh.dev.qs.complaint.entity.ComplaintMeeting;
import pnh.dev.qs.complaint.entity.CustomerComplaint;
import pnh.dev.qs.complaint.enums.ComplaintStatus;
import pnh.dev.qs.complaint.enums.InternalExternal;
import pnh.dev.qs.complaint.repository.ComplaintMeetingRepository;
import pnh.dev.qs.complaint.repository.CustomerComplaintRepository;
import pnh.dev.qs.complaint.service.impl.CustomerComplaintServiceImpl;
import pnh.dev.qs.user.dto.EmailRecipientDTO;
import pnh.dev.qs.user.dto.EmailSendResultDTO;
import pnh.dev.qs.user.dto.MeetingEmailRequest;
import pnh.dev.qs.user.service.UserEmailService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerComplaintServiceTest {

    @Mock
    private CustomerComplaintRepository complaintRepository;

    @Mock
    private ComplaintMeetingRepository meetingRepository;

    @Mock
    private ComplaintTrackingNoGenerator trackingNoGenerator;

    @Mock
    private UserEmailService userEmailService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CustomerComplaintServiceImpl complaintService;

    private UserDetails mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .username("cqe_user@company.com")
                .password("secret")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("Should create complaint with initial status RECEIVED and annual tracking number")
    void testCreateComplaint() {
        LocalDate date = LocalDate.of(2026, 9, 12);
        ComplaintCreateRequest request = ComplaintCreateRequest.builder()
                .receivedDate(date)
                .customerName("Foxconn Technology")
                .model("MDL-9000")
                .issueDescription("Abnormal motor vibration during testing")
                .defectCategory("Mechanical")
                .defectName("Vibration")
                .quantity(5)
                .build();

        when(trackingNoGenerator.generateNextTrackingNo(date)).thenReturn("2026-09-0001");
        when(complaintRepository.save(any(CustomerComplaint.class))).thenAnswer(invocation -> {
            CustomerComplaint c = invocation.getArgument(0);
            return c;
        });

        ComplaintResponse response = complaintService.createComplaint(request, mockUser);

        assertThat(response).isNotNull();
        assertThat(response.getTrackingNo()).isEqualTo("2026-09-0001");
        assertThat(response.getStatus()).isEqualTo(ComplaintStatus.RECEIVED);
        assertThat(response.getYear()).isEqualTo(2026);
        assertThat(response.getMonth()).isEqualTo("09");
        assertThat(response.getModel()).isEqualTo("MDL-9000");
    }

    @Test
    @DisplayName("Should dispatch email, update complaint status to MEETING_SCHEDULED and save meeting record")
    void testScheduleMeetingForComplaint() {
        CustomerComplaint complaint = CustomerComplaint.builder()
                .trackingNo("2026-09-0001")
                .model("MDL-9000")
                .customerName("Foxconn Technology")
                .issueDescription("Abnormal motor vibration")
                .internalExternal(InternalExternal.EXTERNAL)
                .status(ComplaintStatus.RECEIVED)
                .build();

        when(complaintRepository.findById(100L)).thenReturn(Optional.of(complaint));
        when(userEmailService.sendMeetingInvitation(any(MeetingEmailRequest.class)))
                .thenReturn(EmailSendResultDTO.builder()
                        .trackingNo("2026-09-0001")
                        .totalRecipients(1)
                        .toCount(1)
                        .ccCount(0)
                        .message("Sent successfully")
                        .build());

        ComplaintScheduleMeetingRequest request = ComplaintScheduleMeetingRequest.builder()
                .meetingDate(LocalDate.of(2026, 9, 13))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .roomLocation("Conference Room B102")
                .agenda("Preliminary review of motor vibration issue")
                .recipients(List.of(EmailRecipientDTO.builder()
                        .email("engineer@company.com")
                        .name("Engineer")
                        .recipientType("TO")
                        .build()))
                .build();

        EmailSendResultDTO result = complaintService.scheduleMeetingForComplaint(100L, request, mockUser);

        assertThat(result.getTotalRecipients()).isEqualTo(1);
        assertThat(complaint.getStatus()).isEqualTo(ComplaintStatus.MEETING_SCHEDULED);
        verify(complaintRepository).save(complaint);
        verify(meetingRepository).save(any(ComplaintMeeting.class));

        ArgumentCaptor<MeetingEmailRequest> emailCaptor = ArgumentCaptor.forClass(MeetingEmailRequest.class);
        verify(userEmailService).sendMeetingInvitation(emailCaptor.capture());
        MeetingEmailRequest captured = emailCaptor.getValue();
        assertThat(captured.getTrackingNo()).isEqualTo("2026-09-0001");
        assertThat(captured.getModel()).isEqualTo("MDL-9000");
    }

    @Test
    @DisplayName("Should conclude meeting and advance complaint to CONTAINMENT_COMMITTED")
    void testConcludeMeeting_ShouldUpdateMeetingAndAdvanceToContainment() {
        CustomerComplaint complaint = CustomerComplaint.builder()
                .trackingNo("2026-09-0001")
                .status(ComplaintStatus.MEETING_SCHEDULED)
                .receivedDate(LocalDate.of(2026, 9, 11))
                .build();

        ComplaintMeeting meeting = ComplaintMeeting.builder()
                .complaint(complaint)
                .trackingNo("2026-09-0001")
                .meetingDate(LocalDate.of(2026, 9, 12))
                .startTime(LocalTime.of(9, 30))
                .endTime(LocalTime.of(10, 30))
                .roomLocation("A201")
                .agenda("Review")
                .organizerEmail("admin@qs.com")
                .recipientsJson("[]")
                .isConcluded(false)
                .build();

        when(meetingRepository.findById(55L)).thenReturn(Optional.of(meeting));
        when(meetingRepository.save(any(ComplaintMeeting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MeetingConcludeRequest request = MeetingConcludeRequest.builder()
                .conclusion("Khiếu nại hợp lệ - Lỗi bọt khí")
                .minutes("Đã họp với xưởng đúc và QA. Thống nhất cách ly 2 lô.")
                .agreedContainment("Cách ly kho 500 linh kiện và kiểm tra profile nhiệt")
                .transitionToContainment(true)
                .build();

        ComplaintMeetingResponse response = complaintService.concludeMeeting(55L, request, mockUser);

        assertThat(response.getIsConcluded()).isTrue();
        assertThat(response.getConclusion()).isEqualTo("Khiếu nại hợp lệ - Lỗi bọt khí");
        assertThat(response.getMinutes()).contains("Đã họp với xưởng đúc");
        assertThat(response.getAgreedContainment()).contains("Cách ly kho 500 linh kiện");
        assertThat(response.getConcludedBy()).isEqualTo("cqe_user@company.com");

        assertThat(complaint.getStatus()).isEqualTo(ComplaintStatus.CONTAINMENT_COMMITTED);
        assertThat(complaint.getContainmentAction()).isEqualTo("Cách ly kho 500 linh kiện và kiểm tra profile nhiệt");
        verify(complaintRepository).save(complaint);
    }

    @Test
    @DisplayName("exportComplaintsToExcel: Should export xlsx matching Customer complaint template sorted by trackingNo ASC")
    void testExportComplaintsToExcel() throws Exception {
        CustomerComplaint c1 = CustomerComplaint.builder()
                .trackingNo("COMP-2026-001")
                .year(2026)
                .month("Sept")
                .week(37)
                .receivedDate(LocalDate.of(2026, 9, 1))
                .customerName("Honda")
                .model("MD-01")
                .defectName("Scratch")
                .issueDescription("Surface scratch detected")
                .quantity(5)
                .status(ComplaintStatus.RECEIVED)
                .build();

        CustomerComplaint c2 = CustomerComplaint.builder()
                .trackingNo("COMP-2026-002")
                .year(2026)
                .month("Sept")
                .week(37)
                .receivedDate(LocalDate.of(2026, 9, 5))
                .customerName("Toyota")
                .model("MD-02")
                .defectName("Dent")
                .issueDescription("Dent on housing")
                .quantity(10)
                .status(ComplaintStatus.CLOSED)
                .closureDate(LocalDate.of(2026, 9, 10))
                .build();

        when(complaintRepository.findByYearOrderByTrackingNoAsc(2026)).thenReturn(List.of(c1, c2));

        byte[] bytes = complaintService.exportComplaintsToExcel(2026);

        assertThat(bytes).isNotNull();
        assertThat(bytes.length).isGreaterThan(1000);

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(bytes))) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(0);
            assertThat(sheet).isNotNull();
            // Header at row 3 (0-indexed 2)
            assertThat(sheet.getRow(2).getCell(1).getStringCellValue()).isEqualTo("Control No");
            // Row 4 (index 3): COMP-2026-001
            assertThat(sheet.getRow(3).getCell(1).getStringCellValue()).isEqualTo("COMP-2026-001");
            assertThat(sheet.getRow(3).getCell(13).getStringCellValue()).isEqualTo("MD-01");
            // Row 5 (index 4): COMP-2026-002
            assertThat(sheet.getRow(4).getCell(1).getStringCellValue()).isEqualTo("COMP-2026-002");
            assertThat(sheet.getRow(4).getCell(13).getStringCellValue()).isEqualTo("MD-02");
        }
    }
}
