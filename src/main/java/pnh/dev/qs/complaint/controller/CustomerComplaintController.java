package pnh.dev.qs.complaint.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pnh.dev.qs.complaint.dto.ComplaintCreateRequest;
import pnh.dev.qs.complaint.dto.ComplaintMeetingResponse;
import pnh.dev.qs.complaint.dto.ComplaintResponse;
import pnh.dev.qs.complaint.dto.ComplaintScheduleMeetingRequest;
import pnh.dev.qs.complaint.dto.ComplaintSummaryDTO;
import pnh.dev.qs.complaint.dto.ComplaintUpdateRequest;
import pnh.dev.qs.complaint.dto.MeetingConcludeRequest;
import pnh.dev.qs.complaint.enums.ComplaintStatus;
import pnh.dev.qs.complaint.service.CustomerComplaintService;
import pnh.dev.qs.user.dto.EmailSendResultDTO;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class CustomerComplaintController {

    private final CustomerComplaintService complaintService;

    @PostMapping
    public ResponseEntity<ComplaintResponse> createComplaint(
            @Valid @RequestBody ComplaintCreateRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        ComplaintResponse response = complaintService.createComplaint(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ComplaintSummaryDTO>> getComplaints(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) ComplaintStatus status,
            @PageableDefault(size = 20, sort = "receivedDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ComplaintSummaryDTO> page = complaintService.getComplaints(year, month, status, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<ComplaintResponse> getComplaintById(@PathVariable String identifier) {
        ComplaintResponse response = complaintService.getComplaintByIdOrTrackingNo(identifier);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tracking/{trackingNo}")
    public ResponseEntity<ComplaintResponse> getComplaintByTrackingNo(@PathVariable String trackingNo) {
        ComplaintResponse response = complaintService.getComplaintByTrackingNo(trackingNo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/years")
    public ResponseEntity<List<Integer>> getAvailableYears() {
        List<Integer> years = complaintService.getAvailableYears();
        return ResponseEntity.ok(years);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportComplaintsExcel(@RequestParam(required = false) Integer year) {
        byte[] excelBytes = complaintService.exportComplaintsToExcel(year);
        String fileName = "Customer_complaint_template_" + (year != null ? year : "all") + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(excelBytes);
    }

    @PostMapping("/{identifier}/schedule-meeting")
    public ResponseEntity<EmailSendResultDTO> scheduleMeeting(
            @PathVariable String identifier,
            @Valid @RequestBody ComplaintScheduleMeetingRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        EmailSendResultDTO result = complaintService.scheduleMeetingForComplaint(identifier, request, currentUser);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{identifier}")
    public ResponseEntity<ComplaintResponse> updateComplaint(
            @PathVariable String identifier,
            @RequestBody ComplaintUpdateRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        ComplaintResponse response = complaintService.updateComplaint(identifier, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/meetings/{meetingId}/conclude")
    public ResponseEntity<ComplaintMeetingResponse> concludeMeeting(
            @PathVariable Long meetingId,
            @RequestBody MeetingConcludeRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        ComplaintMeetingResponse response = complaintService.concludeMeeting(meetingId, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{identifier}/meetings/{meetingId}/conclude")
    public ResponseEntity<ComplaintResponse> concludeMeetingForComplaint(
            @PathVariable String identifier,
            @PathVariable Long meetingId,
            @RequestBody MeetingConcludeRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        complaintService.concludeMeeting(meetingId, request, currentUser);
        ComplaintResponse updatedComplaint = complaintService.getComplaintByIdOrTrackingNo(identifier);
        return ResponseEntity.ok(updatedComplaint);
    }
}
