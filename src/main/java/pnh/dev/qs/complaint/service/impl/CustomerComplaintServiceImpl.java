package pnh.dev.qs.complaint.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnh.dev.qs.complaint.dto.ComplaintCreateRequest;
import pnh.dev.qs.complaint.dto.ComplaintMeetingResponse;
import pnh.dev.qs.complaint.dto.ComplaintResponse;
import pnh.dev.qs.complaint.dto.ComplaintScheduleMeetingRequest;
import pnh.dev.qs.complaint.dto.ComplaintSummaryDTO;
import pnh.dev.qs.complaint.dto.ComplaintUpdateRequest;
import pnh.dev.qs.complaint.dto.MeetingConcludeRequest;
import pnh.dev.qs.complaint.entity.ComplaintMeeting;
import pnh.dev.qs.complaint.entity.CustomerComplaint;
import pnh.dev.qs.complaint.enums.ComplaintStatus;
import pnh.dev.qs.complaint.enums.InternalExternal;
import pnh.dev.qs.complaint.repository.ComplaintMeetingRepository;
import pnh.dev.qs.complaint.repository.CustomerComplaintRepository;
import pnh.dev.qs.complaint.service.ComplaintTrackingNoGenerator;
import pnh.dev.qs.complaint.service.CustomerComplaintService;
import pnh.dev.qs.exception.custom.ResourceNotFoundException;
import pnh.dev.qs.user.dto.EmailSendResultDTO;
import pnh.dev.qs.user.dto.MeetingEmailRequest;
import pnh.dev.qs.user.service.UserEmailService;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerComplaintServiceImpl implements CustomerComplaintService {

    private final CustomerComplaintRepository complaintRepository;
    private final ComplaintMeetingRepository meetingRepository;
    private final ComplaintTrackingNoGenerator trackingNoGenerator;
    private final UserEmailService userEmailService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ComplaintResponse createComplaint(ComplaintCreateRequest request, UserDetails currentUser) {
        log.info("Creating customer complaint for model: {}, customer: {}", request.getModel(), request.getCustomerName());

        LocalDate receivedDate = request.getReceivedDate() != null ? request.getReceivedDate() : LocalDate.now();
        int year = receivedDate.getYear();
        String monthStr = String.format("%02d", receivedDate.getMonthValue());
        int week = receivedDate.get(WeekFields.ISO.weekOfWeekBasedYear());

        String trackingNo = trackingNoGenerator.generateNextTrackingNo(receivedDate);

        CustomerComplaint complaint = CustomerComplaint.builder()
                .trackingNo(trackingNo)
                .controlNo(request.getControlNo())
                .buildingStage(request.getBuildingStage() != null ? request.getBuildingStage() : "MP")
                .capaNo(request.getCapaNo())
                .year(year)
                .month(monthStr)
                .week(week)
                .receivedDate(receivedDate)
                .originOfComplaint(request.getOriginOfComplaint() != null ? request.getOriginOfComplaint() : "Customer")
                .internalExternal(request.getInternalExternal() != null ? request.getInternalExternal() : InternalExternal.EXTERNAL)
                .salesforceCapa(request.getSalesforceCapa())
                .area(request.getArea())
                .customerName(request.getCustomerName())
                .customerFinding(request.getCustomerFinding())
                .model(request.getModel())
                .issueDescription(request.getIssueDescription())
                .defectCategory(request.getDefectCategory())
                .defectName(request.getDefectName())
                .quantity(request.getQuantity())
                .serialNumbers(request.getSerialNumbers())
                .pictureUrls(request.getPictureUrls())
                .status(ComplaintStatus.RECEIVED)
                .actionStatus("OPEN")
                .build();

        CustomerComplaint saved = complaintRepository.save(complaint);
        log.info("Successfully created complaint ID: {} with trackingNo: {}", saved.getId(), saved.getTrackingNo());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(Long id) {
        CustomerComplaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer complaint not found with ID: " + id));
        return mapToResponse(complaint);
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintByTrackingNo(String trackingNo) {
        CustomerComplaint complaint = complaintRepository.findByTrackingNo(trackingNo)
                .orElseThrow(() -> new ResourceNotFoundException("Customer complaint not found with trackingNo: " + trackingNo));
        return mapToResponse(complaint);
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintByIdOrTrackingNo(String identifier) {
        CustomerComplaint complaint = findComplaintByIdOrTrackingNo(identifier);
        return mapToResponse(complaint);
    }

    private CustomerComplaint findComplaintByIdOrTrackingNo(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new ResourceNotFoundException("Customer complaint identifier must not be empty");
        }
        try {
            Long id = Long.parseLong(identifier.trim());
            Optional<CustomerComplaint> byId = complaintRepository.findById(id);
            if (byId.isPresent()) {
                return byId.get();
            }
        } catch (NumberFormatException ignored) {
            // Not a number, proceed to search by trackingNo
        }

        return complaintRepository.findByTrackingNo(identifier.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Customer complaint not found with ID/TrackingNo: " + identifier));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComplaintSummaryDTO> getComplaints(Integer year, String month, ComplaintStatus status, Pageable pageable) {
        Specification<CustomerComplaint> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (year != null) {
                predicates.add(cb.equal(root.get("year"), year));
            }
            if (month != null && !month.isBlank()) {
                predicates.add(cb.equal(root.get("month"), month));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<CustomerComplaint> page = complaintRepository.findAll(spec, pageable);
        return page.map(this::mapToSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getAvailableYears() {
        return complaintRepository.findDistinctYears();
    }

    @Override
    @Transactional
    public EmailSendResultDTO scheduleMeetingForComplaint(Long complaintId, ComplaintScheduleMeetingRequest request, UserDetails currentUser) {
        return scheduleMeetingForComplaint(String.valueOf(complaintId), request, currentUser);
    }

    @Override
    @Transactional
    public EmailSendResultDTO scheduleMeetingForComplaint(String identifier, ComplaintScheduleMeetingRequest request, UserDetails currentUser) {
        CustomerComplaint complaint = findComplaintByIdOrTrackingNo(identifier);

        log.info("Scheduling preliminary review meeting for complaint: {} ({})", complaint.getTrackingNo(), complaint.getModel());

        // 1. Build MeetingEmailRequest for UserEmailService
        MeetingEmailRequest emailRequest = MeetingEmailRequest.builder()
                .trackingNo(complaint.getTrackingNo())
                .model(complaint.getModel())
                .customerName(complaint.getCustomerName())
                .issueDescription(complaint.getIssueDescription())
                .meetingDate(request.getMeetingDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .roomLocation(request.getRoomLocation())
                .agenda(request.getAgenda())
                .organizerEmail(currentUser != null ? currentUser.getUsername() : null)
                .organizerName(currentUser != null ? currentUser.getUsername() : null)
                .isCustomerInitiated(complaint.getInternalExternal() == InternalExternal.EXTERNAL)
                .recipients(request.getRecipients())
                .build();

        // 2. Dispatch email with .ics calendar attachment
        EmailSendResultDTO emailResult = userEmailService.sendMeetingInvitation(emailRequest);

        // 3. Persist meeting log
        String recipientsJson = "";
        try {
            recipientsJson = objectMapper.writeValueAsString(request.getRecipients());
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize recipients to JSON", e);
        }

        ComplaintMeeting meeting = ComplaintMeeting.builder()
                .complaint(complaint)
                .trackingNo(complaint.getTrackingNo())
                .meetingDate(request.getMeetingDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .roomLocation(request.getRoomLocation())
                .agenda(request.getAgenda())
                .organizerEmail(currentUser.getUsername())
                .organizerName(currentUser.getUsername())
                .recipientsJson(recipientsJson)
                .sentAt(Instant.now())
                .build();

        meetingRepository.save(meeting);

        // 4. Update complaint lifecycle stage if currently RECEIVED
        if (complaint.getStatus() == ComplaintStatus.RECEIVED) {
            complaint.setStatus(ComplaintStatus.MEETING_SCHEDULED);
            complaintRepository.save(complaint);
            log.info("Complaint {} status transitioned to MEETING_SCHEDULED", complaint.getTrackingNo());
        }

        return emailResult;
    }

    @Override
    @Transactional
    public ComplaintMeetingResponse concludeMeeting(Long meetingId, MeetingConcludeRequest request, UserDetails currentUser) {
        ComplaintMeeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + meetingId));

        if (request.getConclusion() != null) {
            meeting.setConclusion(request.getConclusion());
        }
        if (request.getMinutes() != null) {
            meeting.setMinutes(request.getMinutes());
        }
        if (request.getAgreedContainment() != null) {
            meeting.setAgreedContainment(request.getAgreedContainment());
        }

        meeting.setIsConcluded(true);
        meeting.setConcludedAt(Instant.now());
        meeting.setConcludedBy(currentUser != null ? currentUser.getUsername() : "System");

        ComplaintMeeting savedMeeting = meetingRepository.save(meeting);

        CustomerComplaint complaint = meeting.getComplaint();
        if (complaint != null) {
            boolean shouldAdvance = Boolean.TRUE.equals(request.getTransitionToContainment());
            if (request.getAgreedContainment() != null && !request.getAgreedContainment().isBlank()) {
                complaint.setContainmentAction(request.getAgreedContainment());
            }
            if (shouldAdvance && (complaint.getStatus() == ComplaintStatus.RECEIVED || complaint.getStatus() == ComplaintStatus.MEETING_SCHEDULED)) {
                complaint.setStatus(ComplaintStatus.CONTAINMENT_COMMITTED);
            }
            complaintRepository.save(complaint);
            log.info("Complaint {} updated via meeting conclusion. Status: {}", complaint.getTrackingNo(), complaint.getStatus());
        }

        return mapToMeetingResponse(savedMeeting);
    }

    @Override
    @Transactional
    public ComplaintResponse updateComplaint(String identifier, ComplaintUpdateRequest request, UserDetails currentUser) {
        CustomerComplaint complaint = findComplaintByIdOrTrackingNo(identifier);

        if (request.getControlNo() != null) complaint.setControlNo(request.getControlNo());
        if (request.getBuildingStage() != null) complaint.setBuildingStage(request.getBuildingStage());
        if (request.getCapaNo() != null) complaint.setCapaNo(request.getCapaNo());
        if (request.getOriginOfComplaint() != null) complaint.setOriginOfComplaint(request.getOriginOfComplaint());
        if (request.getInternalExternal() != null) complaint.setInternalExternal(request.getInternalExternal());
        if (request.getSalesforceCapa() != null) complaint.setSalesforceCapa(request.getSalesforceCapa());
        if (request.getArea() != null) complaint.setArea(request.getArea());
        if (request.getCustomerName() != null && !request.getCustomerName().isBlank()) complaint.setCustomerName(request.getCustomerName());
        if (request.getCustomerFinding() != null) complaint.setCustomerFinding(request.getCustomerFinding());
        if (request.getModel() != null && !request.getModel().isBlank()) complaint.setModel(request.getModel());
        if (request.getIssueDescription() != null && !request.getIssueDescription().isBlank()) complaint.setIssueDescription(request.getIssueDescription());
        if (request.getDefectCategory() != null) complaint.setDefectCategory(request.getDefectCategory());
        if (request.getDefectName() != null) complaint.setDefectName(request.getDefectName());
        if (request.getQuantity() != null) complaint.setQuantity(request.getQuantity());
        if (request.getSerialNumbers() != null) complaint.setSerialNumbers(request.getSerialNumbers());
        if (request.getPictureUrls() != null) complaint.setPictureUrls(request.getPictureUrls());

        // Phase 3: Containment
        if (request.getContainmentAction() != null) {
            complaint.setContainmentAction(request.getContainmentAction());
        }
        if (request.getContainmentDueDate() != null) {
            complaint.setContainmentDueDate(request.getContainmentDueDate());
        }

        // Phase 4: Root Cause
        if (request.getRootCause() != null) {
            complaint.setRootCause(request.getRootCause());
        }

        // Phase 5 & 6: CAPA
        if (request.getCorrectivePreventiveAction() != null) {
            complaint.setCorrectivePreventiveAction(request.getCorrectivePreventiveAction());
        }
        if (request.getActionOwner() != null) {
            complaint.setActionOwner(request.getActionOwner());
        }
        if (request.getActionDueDate() != null) {
            complaint.setActionDueDate(request.getActionDueDate());
        }
        if (request.getActionStatus() != null) {
            complaint.setActionStatus(request.getActionStatus());
        }

        // Phase 7: Closure
        if (request.getClosureDate() != null) {
            complaint.setClosureDate(request.getClosureDate());
        }
        if (request.getFinalStatus() != null) {
            complaint.setFinalStatus(request.getFinalStatus());
        }
        if (request.getRemarks() != null) {
            complaint.setRemarks(request.getRemarks());
        }

        // Status transition: explicit override or smart progression
        if (request.getStatus() != null) {
            if (complaint.getStatus() == ComplaintStatus.CLOSED && request.getStatus() != ComplaintStatus.CLOSED) {
                // Reopening closed complaint: reset closure date
                complaint.setClosureDate(null);
                log.info("Complaint {} is being REOPENED from CLOSED to {}", complaint.getTrackingNo(), request.getStatus());
            }
            complaint.setStatus(request.getStatus());
        } else {
            // Smart auto-progression based on submitted content if not explicitly provided
            if (complaint.getClosureDate() != null) {
                complaint.setStatus(ComplaintStatus.CLOSED);
            } else if ("VERIFYING".equalsIgnoreCase(complaint.getActionStatus()) || "EFFECTIVENESS_VERIFYING".equalsIgnoreCase(complaint.getActionStatus())) {
                complaint.setStatus(ComplaintStatus.EFFECTIVENESS_VERIFYING);
            } else if (complaint.getCorrectivePreventiveAction() != null && !complaint.getCorrectivePreventiveAction().isBlank()) {
                if (complaint.getStatus().ordinal() < ComplaintStatus.CAPA_COMMITTED.ordinal()) {
                    complaint.setStatus(ComplaintStatus.CAPA_COMMITTED);
                }
            } else if (complaint.getRootCause() != null && !complaint.getRootCause().isBlank()) {
                if (complaint.getStatus().ordinal() < ComplaintStatus.ROOT_CAUSE_ANALYZED.ordinal()) {
                    complaint.setStatus(ComplaintStatus.ROOT_CAUSE_ANALYZED);
                }
            } else if (complaint.getContainmentAction() != null && !complaint.getContainmentAction().isBlank()) {
                if (complaint.getStatus().ordinal() < ComplaintStatus.CONTAINMENT_COMMITTED.ordinal()) {
                    complaint.setStatus(ComplaintStatus.CONTAINMENT_COMMITTED);
                }
            }
        }

        CustomerComplaint saved = complaintRepository.save(complaint);
        log.info("Updated complaint {} ({}) to status: {}", saved.getTrackingNo(), saved.getId(), saved.getStatus());
        return mapToResponse(saved);
    }

    private ComplaintSummaryDTO mapToSummary(CustomerComplaint c) {
        Long ageingClosed = null;
        if (c.getClosureDate() != null) {
            ageingClosed = ChronoUnit.DAYS.between(c.getReceivedDate(), c.getClosureDate());
        }

        Long ageingOpen = null;
        if (c.getStatus() != ComplaintStatus.CLOSED) {
            ageingOpen = ChronoUnit.DAYS.between(c.getReceivedDate(), LocalDate.now());
        }

        return ComplaintSummaryDTO.builder()
                .id(c.getId())
                .trackingNo(c.getTrackingNo())
                .controlNo(c.getControlNo())
                .year(c.getYear())
                .month(c.getMonth())
                .week(c.getWeek())
                .receivedDate(c.getReceivedDate())
                .dueDate(c.getDueDate())
                .customerName(c.getCustomerName())
                .model(c.getModel())
                .issueDescription(c.getIssueDescription())
                .defectCategory(c.getDefectCategory())
                .defectName(c.getDefectName())
                .quantity(c.getQuantity())
                .internalExternal(c.getInternalExternal())
                .status(c.getStatus())
                .actionStatus(c.getActionStatus())
                .finalStatus(c.getFinalStatus())
                .ageingOpen(ageingOpen)
                .ageingClosed(ageingClosed)
                .build();
    }

    private ComplaintResponse mapToResponse(CustomerComplaint c) {
        Long ageingClosed = null;
        if (c.getClosureDate() != null) {
            ageingClosed = ChronoUnit.DAYS.between(c.getReceivedDate(), c.getClosureDate());
        }

        Long ageingOpen = null;
        if (c.getStatus() != ComplaintStatus.CLOSED) {
            ageingOpen = ChronoUnit.DAYS.between(c.getReceivedDate(), LocalDate.now());
        }

        List<ComplaintMeetingResponse> meetingResponses = new ArrayList<>();
        if (c.getMeetings() != null) {
            meetingResponses = c.getMeetings().stream()
                    .map(this::mapToMeetingResponse)
                    .collect(Collectors.toList());
        }

        return ComplaintResponse.builder()
                .id(c.getId())
                .trackingNo(c.getTrackingNo())
                .controlNo(c.getControlNo())
                .buildingStage(c.getBuildingStage())
                .capaNo(c.getCapaNo())
                .year(c.getYear())
                .month(c.getMonth())
                .week(c.getWeek())
                .receivedDate(c.getReceivedDate())
                .dueDate(c.getDueDate())
                .closureDate(c.getClosureDate())
                .originOfComplaint(c.getOriginOfComplaint())
                .internalExternal(c.getInternalExternal())
                .salesforceCapa(c.getSalesforceCapa())
                .area(c.getArea())
                .customerName(c.getCustomerName())
                .customerFinding(c.getCustomerFinding())
                .model(c.getModel())
                .issueDescription(c.getIssueDescription())
                .defectCategory(c.getDefectCategory())
                .defectName(c.getDefectName())
                .quantity(c.getQuantity())
                .serialNumbers(c.getSerialNumbers())
                .pictureUrls(c.getPictureUrls())
                .rootCause(c.getRootCause())
                .containmentAction(c.getContainmentAction())
                .containmentDueDate(c.getContainmentDueDate())
                .correctivePreventiveAction(c.getCorrectivePreventiveAction())
                .actionOwner(c.getActionOwner())
                .actionDueDate(c.getActionDueDate())
                .actionStatus(c.getActionStatus())
                .status(c.getStatus())
                .finalStatus(c.getFinalStatus())
                .remarks(c.getRemarks())
                .ageingOpen(ageingOpen)
                .ageingClosed(ageingClosed)
                .meetings(meetingResponses)
                .createdBy(c.getCreatedBy())
                .createdAt(c.getCreatedAt())
                .updatedBy(c.getUpdatedBy())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private ComplaintMeetingResponse mapToMeetingResponse(ComplaintMeeting m) {
        return ComplaintMeetingResponse.builder()
                .id(m.getId())
                .trackingNo(m.getTrackingNo())
                .meetingDate(m.getMeetingDate())
                .startTime(m.getStartTime())
                .endTime(m.getEndTime())
                .roomLocation(m.getRoomLocation())
                .agenda(m.getAgenda())
                .organizerEmail(m.getOrganizerEmail())
                .organizerName(m.getOrganizerName())
                .recipientsJson(m.getRecipientsJson())
                .sentAt(m.getSentAt())
                .minutes(m.getMinutes())
                .conclusion(m.getConclusion())
                .agreedContainment(m.getAgreedContainment())
                .isConcluded(m.getIsConcluded())
                .concludedAt(m.getConcludedAt())
                .concludedBy(m.getConcludedBy())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportComplaintsToExcel(Integer year) {
        log.info("Exporting customer complaints to Excel template for year: {}", year);

        // Fetch complaints ordered strictly by trackingNo ascending
        List<CustomerComplaint> complaints;
        if (year != null) {
            complaints = complaintRepository.findByYearOrderByTrackingNoAsc(year);
        } else {
            complaints = complaintRepository.findAllByOrderByTrackingNoAsc();
        }

        Workbook workbook = null;
        try {
            // Load template from resources or filesystem
            InputStream is = getClass().getResourceAsStream("/templates/Customer_complaint_template.xlsx");
            if (is == null) {
                File fallback = new File("d:/Intern-2026/qs-system/file/Customer complaint template.xlsx");
                if (fallback.exists()) {
                    is = new FileInputStream(fallback);
                } else {
                    File fallback2 = new File("file/Customer complaint template.xlsx");
                    if (fallback2.exists()) {
                        is = new FileInputStream(fallback2);
                    }
                }
            }

            if (is != null) {
                workbook = new XSSFWorkbook(is);
            } else {
                workbook = new XSSFWorkbook();
                workbook.createSheet("Customer Complaints");
            }

            Sheet sheet = workbook.getSheetAt(0);

            // Setup common styling for data cells
            CellStyle defaultDataStyle = workbook.createCellStyle();
            defaultDataStyle.setBorderTop(BorderStyle.THIN);
            defaultDataStyle.setBorderBottom(BorderStyle.THIN);
            defaultDataStyle.setBorderLeft(BorderStyle.THIN);
            defaultDataStyle.setBorderRight(BorderStyle.THIN);
            defaultDataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            Font font = workbook.createFont();
            font.setFontName("Calibri");
            font.setFontHeightInPoints((short) 10);
            defaultDataStyle.setFont(font);

            int startRow = 3; // Row 4 in Excel (0-indexed 3)

            // Remove any existing rows from startRow onwards
            int lastRowNum = sheet.getLastRowNum();
            for (int r = lastRowNum; r >= startRow; r--) {
                Row oldRow = sheet.getRow(r);
                if (oldRow != null) {
                    sheet.removeRow(oldRow);
                }
            }

            LocalDate today = LocalDate.now();

            for (int i = 0; i < complaints.size(); i++) {
                CustomerComplaint c = complaints.get(i);
                Row row = sheet.createRow(startRow + i);

                // Col 0: No.
                createNumericCell(row, 0, i + 1, defaultDataStyle);

                // Col 1: Control No (trackingNo)
                createStringCell(row, 1, c.getTrackingNo() != null ? c.getTrackingNo() : c.getControlNo(), defaultDataStyle);

                // Col 2: Building Stage
                createStringCell(row, 2, c.getBuildingStage(), defaultDataStyle);

                // Col 3: CAPA No:
                createStringCell(row, 3, c.getCapaNo(), defaultDataStyle);

                // Col 4: Year
                if (c.getYear() != null) {
                    createNumericCell(row, 4, c.getYear(), defaultDataStyle);
                } else if (c.getReceivedDate() != null) {
                    createNumericCell(row, 4, c.getReceivedDate().getYear(), defaultDataStyle);
                } else {
                    createStringCell(row, 4, "", defaultDataStyle);
                }

                // Col 5: Month
                createStringCell(row, 5, c.getMonth(), defaultDataStyle);

                // Col 6: Week
                if (c.getWeek() != null) {
                    createNumericCell(row, 6, c.getWeek(), defaultDataStyle);
                } else if (c.getReceivedDate() != null) {
                    int w = c.getReceivedDate().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                    createNumericCell(row, 6, w, defaultDataStyle);
                } else {
                    createStringCell(row, 6, "", defaultDataStyle);
                }

                // Col 7: Received Date
                createStringCell(row, 7, c.getReceivedDate() != null ? c.getReceivedDate().toString() : "", defaultDataStyle);

                // Col 8: Origin of Complaint
                createStringCell(row, 8, c.getOriginOfComplaint(), defaultDataStyle);

                // Col 9: Internal/External
                createStringCell(row, 9, c.getInternalExternal() != null ? c.getInternalExternal().name() : "", defaultDataStyle);

                // Col 10: Salesforce CAPA
                createStringCell(row, 10, c.getSalesforceCapa(), defaultDataStyle);

                // Col 11: Area
                createStringCell(row, 11, c.getArea(), defaultDataStyle);

                // Col 12: Customer finding
                createStringCell(row, 12, c.getCustomerFinding(), defaultDataStyle);

                // Col 13: Model
                createStringCell(row, 13, c.getModel(), defaultDataStyle);

                // Col 14: Issue Description
                createStringCell(row, 14, c.getIssueDescription(), defaultDataStyle);

                // Col 15: Defect Category
                createStringCell(row, 15, c.getDefectCategory(), defaultDataStyle);

                // Col 16: Defect Name
                createStringCell(row, 16, c.getDefectName(), defaultDataStyle);

                // Col 17: Q'ty (EA)
                if (c.getQuantity() != null) {
                    createNumericCell(row, 17, c.getQuantity(), defaultDataStyle);
                } else {
                    createNumericCell(row, 17, 1, defaultDataStyle);
                }

                // Col 18: SN
                createStringCell(row, 18, c.getSerialNumbers(), defaultDataStyle);

                // Col 19: Picture
                createStringCell(row, 19, c.getPictureUrls(), defaultDataStyle);

                // Col 20: Root Cause
                createStringCell(row, 20, c.getRootCause(), defaultDataStyle);

                // Col 21: Containment Action
                createStringCell(row, 21, c.getContainmentAction(), defaultDataStyle);

                // Col 22: Corrective and Preventive Action
                createStringCell(row, 22, c.getCorrectivePreventiveAction(), defaultDataStyle);

                // Col 23: Action Owner
                createStringCell(row, 23, c.getActionOwner(), defaultDataStyle);

                // Col 24: Due Date
                LocalDate due = c.getActionDueDate() != null ? c.getActionDueDate() : c.getDueDate();
                createStringCell(row, 24, due != null ? due.toString() : "", defaultDataStyle);

                // Col 25: Action Status
                createStringCell(row, 25, c.getActionStatus(), defaultDataStyle);

                // Col 26: Closure Date
                createStringCell(row, 26, c.getClosureDate() != null ? c.getClosureDate().toString() : "", defaultDataStyle);

                // Col 27: Final Status
                String finalStat = c.getFinalStatus();
                if (finalStat == null || finalStat.isBlank()) {
                    finalStat = c.getStatus() == ComplaintStatus.CLOSED ? "Closed" : "Open";
                }
                createStringCell(row, 27, finalStat, defaultDataStyle);

                // Col 28: Remarks
                createStringCell(row, 28, c.getRemarks(), defaultDataStyle);

                // Col 29: Current Date
                createStringCell(row, 29, today.toString(), defaultDataStyle);

                // Col 30: Ageing Closed
                if (c.getStatus() == ComplaintStatus.CLOSED && c.getReceivedDate() != null && c.getClosureDate() != null) {
                    long daysClosed = ChronoUnit.DAYS.between(c.getReceivedDate(), c.getClosureDate());
                    createNumericCell(row, 30, Math.max(0, daysClosed), defaultDataStyle);
                } else {
                    createStringCell(row, 30, "", defaultDataStyle);
                }

                // Col 31: Ageing Open
                if (c.getStatus() != ComplaintStatus.CLOSED && c.getReceivedDate() != null) {
                    long daysOpen = ChronoUnit.DAYS.between(c.getReceivedDate(), today);
                    createNumericCell(row, 31, Math.max(0, daysOpen), defaultDataStyle);
                } else {
                    createStringCell(row, 31, "", defaultDataStyle);
                }
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to export complaints to Excel", e);
            throw new RuntimeException("Failed to export customer complaints to Excel: " + e.getMessage(), e);
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void createStringCell(Row row, int colIndex, String value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private void createNumericCell(Row row, int colIndex, double value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
