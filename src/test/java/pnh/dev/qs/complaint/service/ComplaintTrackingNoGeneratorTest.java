package pnh.dev.qs.complaint.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pnh.dev.qs.complaint.repository.CustomerComplaintRepository;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplaintTrackingNoGeneratorTest {

    @Mock
    private CustomerComplaintRepository complaintRepository;

    @InjectMocks
    private ComplaintTrackingNoGenerator generator;

    @Test
    @DisplayName("Should generate 2026-09-0001 for first complaint in September 2026")
    void testFirstComplaintInYear() {
        LocalDate date = LocalDate.of(2026, 9, 12);
        when(complaintRepository.findMaxSequenceByYear(2026)).thenReturn(0);

        String trackingNo = generator.generateNextTrackingNo(date);

        assertThat(trackingNo).isEqualTo("2026-09-0001");
        verify(complaintRepository).findMaxSequenceByYear(2026);
    }

    @Test
    @DisplayName("Should increment annual sequence continuously across different months in 2026")
    void testSequentialAcrossMonthsInSameYear() {
        LocalDate dateOct = LocalDate.of(2026, 10, 5);
        when(complaintRepository.findMaxSequenceByYear(2026)).thenReturn(2);

        String trackingNo = generator.generateNextTrackingNo(dateOct);

        assertThat(trackingNo).isEqualTo("2026-10-0003");
    }

    @Test
    @DisplayName("Should reset sequence to 0001 when entering new year 2027")
    void testResetOnNewYear() {
        LocalDate date2027 = LocalDate.of(2027, 1, 15);
        when(complaintRepository.findMaxSequenceByYear(2027)).thenReturn(0);

        String trackingNo = generator.generateNextTrackingNo(date2027);

        assertThat(trackingNo).isEqualTo("2027-01-0001");
        verify(complaintRepository).findMaxSequenceByYear(2027);
    }
}
