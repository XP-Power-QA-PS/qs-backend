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
public class CapaNoGenerator {

    private final CustomerComplaintRepository complaintRepository;

    /**
     * Generates an annual sequential CAPA number in format: CAPA-YYYY-XXX
     * Example: CAPA-2026-001
     * The sequence number XXX increments annually across the entire year.
     *
     * @param date the reference date (e.g. receivedDate or today)
     * @return generated CAPA number
     */
    @Transactional(readOnly = true)
    public synchronized String generateNextCapaNo(LocalDate date) {
        int year = (date != null) ? date.getYear() : LocalDate.now().getYear();

        Integer maxSeq = complaintRepository.findMaxCapaSequenceByYear(year);
        int nextSeq = (maxSeq != null ? maxSeq : 0) + 1;

        String capaNo = String.format("CAPA-%d-%03d", year, nextSeq);
        log.info("Generated CAPA number: {} for year: {}", capaNo, year);
        return capaNo;
    }
}
