package com.medev.hrstream.candidate.profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateExperienceRepository extends JpaRepository<CandidateExperience, String> {
    List<CandidateExperience> findByCandidateId(String candidateId);
}
