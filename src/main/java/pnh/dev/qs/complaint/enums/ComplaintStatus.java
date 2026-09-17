package pnh.dev.qs.complaint.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ComplaintStatus {
    RECEIVED("New Intake", 1),
    MEETING_SCHEDULED("Meeting Scheduled", 2),
    CONTAINMENT_COMMITTED("Containment Committed", 3),
    ROOT_CAUSE_ANALYZED("Root Cause Identified", 4),
    CAPA_COMMITTED("CAPA Committed", 5),
    EFFECTIVENESS_VERIFYING("Effectiveness Verifying", 6),
    CLOSED("Case Closed", 7);

    private final String displayName;
    private final int stageNumber;
}


