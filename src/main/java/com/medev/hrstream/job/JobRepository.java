package com.medev.hrstream.job;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, String> {

    @Modifying
    @Query("UPDATE Job j SET j.deleted = true WHERE j.id = ?1")
    void softdeleteById(String jobId);

    @Query("SELECT j FROM Job j WHERE j.deleted = false")
    Page<Job> findAllActive(Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.deleted = false AND j.status = 'OPEN'")
    Page<Job> findAllOpenJobs(Pageable pageable);

    Optional<Job> findByApplicationToken(String token);

    /** Row-level lock used by cap-close to serialise concurrent checks. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT j FROM Job j WHERE j.id = :id")
    Optional<Job> findByIdForUpdate(@Param("id") String id);

    /** Returns OPEN jobs whose deadline has passed. */
    @Query("SELECT j FROM Job j WHERE j.deleted = false AND j.status = 'OPEN' " +
           "AND j.dateLimte IS NOT NULL AND j.dateLimte < :now")
    List<Job> findOpenJobsWithDeadlineBefore(@Param("now") LocalDateTime now);

    @Query("SELECT j FROM Job j WHERE j.deleted = false " +
           "AND (:hasStatuses = false OR j.status IN :statuses) " +
           "AND (:department IS NULL OR j.department = :department) " +
           "AND (:location IS NULL OR j.location = :location) " +
           "AND (:closedReason IS NULL OR j.closedReason = :closedReason) " +
           "AND (:search IS NULL OR LOWER(j.title) LIKE :search)")
    Page<Job> findAllActiveFiltered(
            @Param("statuses") List<JobStatus> statuses,
            @Param("hasStatuses") boolean hasStatuses,
            @Param("department") String department,
            @Param("location") String location,
            @Param("closedReason") com.medev.hrstream.job.lifecycle.ClosedReason closedReason,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT DISTINCT j.department FROM Job j WHERE j.deleted = false AND j.department IS NOT NULL")
    List<String> findUniqueDepartments();

    @Query("SELECT DISTINCT j.location FROM Job j WHERE j.deleted = false AND j.location IS NOT NULL")
    List<String> findUniqueLocations();

    long countByDeletedFalse();

    long countByDeletedFalseAndStatus(JobStatus status);
}
