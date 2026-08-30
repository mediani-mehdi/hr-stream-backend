package com.medev.hrstream.candidate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, String> {
    @EntityGraph(attributePaths = {"education", "experience", "skills", "languages"})
    Optional<Candidate> findByEmail(String email);
}

