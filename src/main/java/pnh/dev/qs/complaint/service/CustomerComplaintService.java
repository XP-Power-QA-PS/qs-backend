package pnh.dev.qs.complaint.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import pnh.dev.qs.complaint.dto.ComplaintCreateRequest;
import pnh.dev.qs.complaint.dto.ComplaintResponse;
import pnh.dev.qs.complaint.dto.ComplaintScheduleMeetingRequest;
import pnh.dev.qs.complaint.dto.ComplaintSummaryDTO;
import pnh.dev.qs.complaint.dto.ComplaintUpdateRequest;
import pnh.dev.qs.complaint.enums.ComplaintStatus;
import pnh.dev.qs.user.dto.EmailSendResultDTO;

import java.util.List;

import pnh.dev.qs.complaint.dto.ComplaintMeetingResponse;
import pnh.dev.qs.complaint.dto.MeetingConcludeRequest;

public interface CustomerComplaintService {

    ComplaintResponse createComplaint(ComplaintCreateRequest request, UserDetails currentUser);

    ComplaintResponse getComplaintById(Long id);

    ComplaintResponse getComplaintByTrackingNo(String trackingNo);

    ComplaintResponse getComplaintByIdOrTrackingNo(String identifier);

    Page<ComplaintSummaryDTO> getComplaints(Integer year, String month, ComplaintStatus status, Pageable pageable);

    List<Integer> getAvailableYears();

    EmailSendResultDTO scheduleMeetingForComplaint(Long complaintId, ComplaintScheduleMeetingRequest request, UserDetails currentUser);

    EmailSendResultDTO scheduleMeetingForComplaint(String identifier, ComplaintScheduleMeetingRequest request, UserDetails currentUser);

    ComplaintResponse updateComplaint(String identifier, ComplaintUpdateRequest request, UserDetails currentUser);

    ComplaintMeetingResponse concludeMeeting(Long meetingId, MeetingConcludeRequest request, UserDetails currentUser);
    byte[] exportComplaintsToExcel(Integer year);
}
