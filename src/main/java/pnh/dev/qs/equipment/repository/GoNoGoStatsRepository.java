package pnh.dev.qs.equipment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pnh.dev.qs.equipment.entity.EquipmentTestAttempt;

import java.time.Instant;
import java.util.List;

@Repository
public interface GoNoGoStatsRepository extends JpaRepository<EquipmentTestAttempt, Long> {

    // ─── 1. Summary KPI ──────────────────────────────────────────────────────

    /**
     * Tổng hợp các chỉ số KPI.
     * Trả về List<Object[]> gồm 1 row:
     * [totalAttempts, passCount, failCount, goFailCount, noGoFailCount, programFailCount]
     * <p>
     * Dùng COALESCE để tránh NULL khi không có data.
     * Tách thành 2 overload để tránh lỗi binding NULL với parameter PostgreSQL.
     */
    @Query(value = """
            SELECT
                COUNT(a.id)                                                      AS totalAttempts,
                COUNT(CASE WHEN a.result_status = 'PASS' THEN 1 END)            AS passCount,
                COUNT(CASE WHEN a.result_status = 'FAIL' THEN 1 END)            AS failCount,
                COUNT(CASE WHEN a.go_status      = 'FAIL' THEN 1 END)            AS goFailCount,
                COUNT(CASE WHEN a.no_go_status   = 'FAIL' THEN 1 END)            AS noGoFailCount,
                COUNT(CASE WHEN a.program_status = 'FAIL' THEN 1 END)            AS programFailCount
            FROM equipment_test_attempts a
            JOIN equipment_daily_tests   dt ON a.daily_test_id = dt.id
            JOIN equipment_test_records  tr ON dt.record_id    = tr.id
            JOIN equipments              e  ON tr.equipment_id = e.id
            WHERE a.attempt_time BETWEEN :startDate AND :endDate
              AND a.deleted_at IS NULL
            """, nativeQuery = true)
    List<Object[]> getRawSummaryAllFloors(@Param("startDate") Instant startDate,
                                          @Param("endDate") Instant endDate);

    @Query(value = """
            SELECT
                COUNT(a.id)                                                      AS totalAttempts,
                COUNT(CASE WHEN a.result_status = 'PASS' THEN 1 END)            AS passCount,
                COUNT(CASE WHEN a.result_status = 'FAIL' THEN 1 END)            AS failCount,
                COUNT(CASE WHEN a.go_status      = 'FAIL' THEN 1 END)            AS goFailCount,
                COUNT(CASE WHEN a.no_go_status   = 'FAIL' THEN 1 END)            AS noGoFailCount,
                COUNT(CASE WHEN a.program_status = 'FAIL' THEN 1 END)            AS programFailCount
            FROM equipment_test_attempts a
            JOIN equipment_daily_tests   dt ON a.daily_test_id = dt.id
            JOIN equipment_test_records  tr ON dt.record_id    = tr.id
            JOIN equipments              e  ON tr.equipment_id = e.id
            WHERE a.attempt_time BETWEEN :startDate AND :endDate
              AND e.floor_id = :floorId
              AND a.deleted_at IS NULL
            """, nativeQuery = true)
    List<Object[]> getRawSummaryByFloor(@Param("startDate") Instant startDate,
                                         @Param("endDate") Instant endDate,
                                         @Param("floorId") Long floorId);

    // ─── 1b. Critical / Tested equipment counts ───────────────────────────────

    @Query(value = """
            SELECT COUNT(DISTINCT e.id)
            FROM equipment_test_attempts a
            JOIN equipment_daily_tests   dt ON a.daily_test_id = dt.id
            JOIN equipment_test_records  tr ON dt.record_id    = tr.id
            JOIN equipments              e  ON tr.equipment_id = e.id
            WHERE a.attempt_time BETWEEN :startDate AND :endDate
              AND a.result_status = 'FAIL'
              AND a.deleted_at IS NULL
            """, nativeQuery = true)
    long countCriticalEquipmentsAllFloors(@Param("startDate") Instant startDate,
                                           @Param("endDate") Instant endDate);

    @Query(value = """
            SELECT COUNT(DISTINCT e.id)
            FROM equipment_test_attempts a
            JOIN equipment_daily_tests   dt ON a.daily_test_id = dt.id
            JOIN equipment_test_records  tr ON dt.record_id    = tr.id
            JOIN equipments              e  ON tr.equipment_id = e.id
            WHERE a.attempt_time BETWEEN :startDate AND :endDate
              AND e.floor_id = :floorId
              AND a.result_status = 'FAIL'
              AND a.deleted_at IS NULL
            """, nativeQuery = true)
    long countCriticalEquipmentsByFloor(@Param("startDate") Instant startDate,
                                         @Param("endDate") Instant endDate,
                                         @Param("floorId") Long floorId);

    @Query(value = """
            SELECT COUNT(DISTINCT e.id)
            FROM equipment_test_attempts a
            JOIN equipment_daily_tests   dt ON a.daily_test_id = dt.id
            JOIN equipment_test_records  tr ON dt.record_id    = tr.id
            JOIN equipments              e  ON tr.equipment_id = e.id
            WHERE a.attempt_time BETWEEN :startDate AND :endDate
              AND a.deleted_at IS NULL
            """, nativeQuery = true)
    long countTestedEquipmentsAllFloors(@Param("startDate") Instant startDate,
                                         @Param("endDate") Instant endDate);

    @Query(value = """
            SELECT COUNT(DISTINCT e.id)
            FROM equipment_test_attempts a
            JOIN equipment_daily_tests   dt ON a.daily_test_id = dt.id
            JOIN equipment_test_records  tr ON dt.record_id    = tr.id
            JOIN equipments              e  ON tr.equipment_id = e.id
            WHERE a.attempt_time BETWEEN :startDate AND :endDate
              AND e.floor_id = :floorId
              AND a.deleted_at IS NULL
            """, nativeQuery = true)
    long countTestedEquipmentsByFloor(@Param("startDate") Instant startDate,
                                       @Param("endDate") Instant endDate,
                                       @Param("floorId") Long floorId);

    // ─── 2. Daily Trend ───────────────────────────────────────────────────────

    @Query(value = """
            SELECT
                DATE(a.attempt_time AT TIME ZONE 'UTC')                          AS testDate,
                COUNT(a.id)                                                      AS totalAttempts,
                COUNT(CASE WHEN a.result_status = 'PASS' THEN 1 END)            AS passCount,
                COUNT(CASE WHEN a.result_status = 'FAIL' THEN 1 END)            AS failCount
            FROM equipment_test_attempts a
            JOIN equipment_daily_tests   dt ON a.daily_test_id = dt.id
            JOIN equipment_test_records  tr ON dt.record_id    = tr.id
            JOIN equipments              e  ON tr.equipment_id = e.id
            WHERE a.attempt_time BETWEEN :startDate AND :endDate
              AND a.deleted_at IS NULL
            GROUP BY DATE(a.attempt_time AT TIME ZONE 'UTC')
            ORDER BY testDate
            """, nativeQuery = true)
    List<Object[]> getRawDailyTrendAllFloors(@Param("startDate") Instant startDate,
                                              @Param("endDate") Instant endDate);

    @Query(value = """
            SELECT
                DATE(a.attempt_time AT TIME ZONE 'UTC')                          AS testDate,
                COUNT(a.id)                                                      AS totalAttempts,
                COUNT(CASE WHEN a.result_status = 'PASS' THEN 1 END)            AS passCount,
                COUNT(CASE WHEN a.result_status = 'FAIL' THEN 1 END)            AS failCount
            FROM equipment_test_attempts a
            JOIN equipment_daily_tests   dt ON a.daily_test_id = dt.id
            JOIN equipment_test_records  tr ON dt.record_id    = tr.id
            JOIN equipments              e  ON tr.equipment_id = e.id
            WHERE a.attempt_time BETWEEN :startDate AND :endDate
              AND e.floor_id = :floorId
              AND a.deleted_at IS NULL
            GROUP BY DATE(a.attempt_time AT TIME ZONE 'UTC')
            ORDER BY testDate
            """, nativeQuery = true)
    List<Object[]> getRawDailyTrendByFloor(@Param("startDate") Instant startDate,
                                            @Param("endDate") Instant endDate,
                                            @Param("floorId") Long floorId);

    // ─── 3. By Floor ──────────────────────────────────────────────────────────

    @Query(value = """
            SELECT
                f.id                                                              AS floorId,
                f.name                                                            AS floorName,
                COUNT(a.id)                                                      AS totalAttempts,
                COUNT(CASE WHEN a.result_status = 'PASS' THEN 1 END)            AS passCount,
                COUNT(CASE WHEN a.result_status = 'FAIL' THEN 1 END)            AS failCount,
                COUNT(DISTINCT e.id)                                              AS testedEquipments
            FROM equipment_test_attempts a
            JOIN equipment_daily_tests   dt ON a.daily_test_id = dt.id
            JOIN equipment_test_records  tr ON dt.record_id    = tr.id
            JOIN equipments              e  ON tr.equipment_id = e.id
            JOIN floors                  f  ON e.floor_id      = f.id
            WHERE a.attempt_time BETWEEN :startDate AND :endDate
              AND a.deleted_at IS NULL
            GROUP BY f.id, f.name
            ORDER BY passCount DESC
            """, nativeQuery = true)
    List<Object[]> getRawStatsByFloor(@Param("startDate") Instant startDate,
                                       @Param("endDate") Instant endDate);

    // ─── 4. Worst Equipments ─────────────────────────────────────────────────

    @Query(value = """
            SELECT
                e.id                                                              AS equipmentId,
                e.equipment_code                                                  AS equipmentCode,
                e.equipment_name                                                  AS equipmentName,
                f.name                                                            AS floorName,
                COUNT(a.id)                                                      AS totalAttempts,
                COUNT(CASE WHEN a.result_status = 'PASS' THEN 1 END)            AS passCount,
                COUNT(CASE WHEN a.result_status = 'FAIL' THEN 1 END)            AS failCount,
                COUNT(DISTINCT dt.id)                                            AS totalDays
            FROM equipment_test_attempts a
            JOIN equipment_daily_tests   dt ON a.daily_test_id = dt.id
            JOIN equipment_test_records  tr ON dt.record_id    = tr.id
            JOIN equipments              e  ON tr.equipment_id = e.id
            JOIN floors                  f  ON e.floor_id      = f.id
            WHERE a.attempt_time BETWEEN :startDate AND :endDate
              AND a.deleted_at IS NULL
            GROUP BY e.id, e.equipment_code, e.equipment_name, f.name
            HAVING COUNT(CASE WHEN a.result_status = 'FAIL' THEN 1 END) > 0
            ORDER BY failCount DESC
            LIMIT :limitCount
            """, nativeQuery = true)
    List<Object[]> getRawWorstEquipments(@Param("startDate") Instant startDate,
                                          @Param("endDate") Instant endDate,
                                          @Param("limitCount") int limitCount);

    // ─── 5. Per-Equipment: Daily breakdown ────────────────────────────────────

    @Query(value = """
            SELECT
                dt.test_date                                                      AS testDate,
                COUNT(a.id)                                                      AS totalAttempts,
                COUNT(CASE WHEN a.result_status = 'PASS' THEN 1 END)            AS passCount,
                COUNT(CASE WHEN a.result_status = 'FAIL' THEN 1 END)            AS failCount,
                COUNT(CASE WHEN a.go_status      = 'FAIL' THEN 1 END)            AS goFailCount,
                COUNT(CASE WHEN a.no_go_status   = 'FAIL' THEN 1 END)            AS noGoFailCount,
                COUNT(CASE WHEN a.program_status = 'FAIL' THEN 1 END)            AS programFailCount
            FROM equipment_test_attempts a
            JOIN equipment_daily_tests   dt ON a.daily_test_id = dt.id
            JOIN equipment_test_records  tr ON dt.record_id    = tr.id
            WHERE tr.equipment_id = :equipmentId
              AND tr.test_month   = :month
              AND tr.test_year    = :year
              AND a.deleted_at IS NULL
            GROUP BY dt.test_date
            ORDER BY dt.test_date
            """, nativeQuery = true)
    List<Object[]> getRawDailyBreakdownForEquipment(@Param("equipmentId") Long equipmentId,
                                                     @Param("month") int month,
                                                     @Param("year") int year);

    // ─── 6. Per-Equipment: Monthly trend ─────────────────────────────────────

    @Query(value = """
            SELECT
                tr.test_month                                                     AS testMonth,
                tr.test_year                                                      AS testYear,
                COUNT(a.id)                                                      AS totalAttempts,
                COUNT(CASE WHEN a.result_status = 'PASS' THEN 1 END)            AS passCount,
                COUNT(CASE WHEN a.result_status = 'FAIL' THEN 1 END)            AS failCount,
                COUNT(DISTINCT dt.id)                                            AS totalDays
            FROM equipment_test_attempts a
            JOIN equipment_daily_tests   dt ON a.daily_test_id = dt.id
            JOIN equipment_test_records  tr ON dt.record_id    = tr.id
            WHERE tr.equipment_id = :equipmentId
              AND a.deleted_at IS NULL
            GROUP BY tr.test_month, tr.test_year
            ORDER BY tr.test_year , tr.test_month
            """, nativeQuery = true)
    List<Object[]> getRawMonthlyTrendForEquipment(@Param("equipmentId") Long equipmentId);
}
