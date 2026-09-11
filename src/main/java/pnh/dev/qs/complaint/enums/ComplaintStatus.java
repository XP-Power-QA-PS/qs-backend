package pnh.dev.qs.complaint.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ComplaintStatus {
    RECEIVED("Mới tiếp nhận / Chờ họp sơ bộ", 1),
    MEETING_SCHEDULED("Đã lên lịch họp sơ bộ / Chờ Containment", 2),
    CONTAINMENT_COMMITTED("Đã có biện pháp Containment / Đang tìm Root Cause", 3),
    ROOT_CAUSE_ANALYZED("Đã xác định Root Cause / Chờ giải pháp CAPA", 4),
    CAPA_COMMITTED("Đã cam kết CAPA / Đang theo dõi hiệu quả", 5),
    EFFECTIVENESS_VERIFYING("Đang thẩm định hiệu quả (30 ngày)", 6),
    CLOSED("Đã đóng hồ sơ", 7);

    private final String displayName;
    private final int stageNumber;
}
