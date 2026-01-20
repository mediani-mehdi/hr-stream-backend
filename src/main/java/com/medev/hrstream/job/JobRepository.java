package com.medev.hrstream.job;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, String> {

    @Modifying
    @Query("UPDATE Job j SET j.deleted = true WHERE j.id = ?1")
    void softdeleteById(String jobId);

    //@Query("SELECT j FROM Job j WHERE j.deleted = false AND j.status IN ?2")
    //Page<Job> findAll(Pageable pageable, List<JobStatus> statuses);
}
