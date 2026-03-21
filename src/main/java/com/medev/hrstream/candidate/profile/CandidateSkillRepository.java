package com.medev.hrstream.candidate.profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateSkillRepository extends JpaRepository<CandidateSkill, String> {
    List<CandidateSkill> findByCandidateId(String candidateId);
}
