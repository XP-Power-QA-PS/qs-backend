package pnh.dev.qs.complaint.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pnh.dev.qs.complaint.entity.CustomerComplaint;
import pnh.dev.qs.complaint.enums.ComplaintStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerComplaintRepository extends JpaRepository<CustomerComplaint, Long>, JpaSpecificationExecutor<CustomerComplaint> {

    Optional<CustomerComplaint> findByTrackingNo(String trackingNo);

    boolean existsByTrackingNo(String trackingNo);

    @Query(value = "SELECT COALESCE(MAX(CAST(SPLIT_PART(tracking_no, '-', 3) AS INTEGER)), 0) " +
                   "FROM customer_complaints WHERE year = :year", nativeQuery = true)
    Integer findMaxSequenceByYear(@Param("year") Integer year);

    Page<CustomerComplaint> findByYear(Integer year, Pageable pageable);

    Page<CustomerComplaint> findByYearAndStatus(Integer year, ComplaintStatus status, Pageable pageable);

    @Query("SELECT DISTINCT c.year FROM CustomerComplaint c ORDER BY c.year DESC")
    List<Integer> findDistinctYears();

    List<CustomerComplaint> findByYearOrderByTrackingNoAsc(Integer year);

    List<CustomerComplaint> findAllByOrderByTrackingNoAsc();
}
