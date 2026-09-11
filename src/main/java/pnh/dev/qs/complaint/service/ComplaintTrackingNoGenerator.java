package pnh.dev.qs.complaint.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pnh.dev.qs.complaint.repository.CustomerComplaintRepository;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ComplaintTrackingNoGenerator {

    private final CustomerComplaintRepository complaintRepository;

    /**
     * Generates an annual sequential tracking number in format: YYYY-MM-ZZZZ
     * Example: 2026-09-0001
     * The sequence number ZZZZ increments annually across the entire year, resetting on new years.
     *
     * @param receivedDate the date the complaint was received
     * @return generated tracking number
     */
    @Transactional(readOnly = true)
    public synchronized String generateNextTrackingNo(LocalDate receivedDate) {
        int year = receivedDate.getYear();
        String monthStr = String.format("%02d", receivedDate.getMonthValue());

        Integer maxSeq = complaintRepository.findMaxSequenceByYear(year);
        int nextSeq = (maxSeq != null ? maxSeq : 0) + 1;

        String trackingNo = String.format("%d-%s-%04d", year, monthStr, nextSeq);
        log.info("Generated complaint tracking number: {} for year: {}", trackingNo, year);
        return trackingNo;
    }
}
