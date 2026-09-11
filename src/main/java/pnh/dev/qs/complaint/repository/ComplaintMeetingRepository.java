package pnh.dev.qs.complaint.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pnh.dev.qs.complaint.entity.ComplaintMeeting;

import java.util.List;

@Repository
public interface ComplaintMeetingRepository extends JpaRepository<ComplaintMeeting, Long> {

    List<ComplaintMeeting> findByComplaintIdOrderBySentAtDesc(Long complaintId);

    List<ComplaintMeeting> findByTrackingNoOrderBySentAtDesc(String trackingNo);
}
