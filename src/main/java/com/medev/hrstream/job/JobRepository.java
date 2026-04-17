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
}
