package com.medev.hrstream.candidate.profile;

import com.medev.hrstream.candidate.Candidate;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "candidate_skills")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CandidateSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    private String name;

    @Enumerated(EnumType.STRING)
    private SkillLevel level;
}
