package com.medev.hrstream.candidate.profile;

import com.medev.hrstream.candidate.Candidate;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "candidate_education")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CandidateEducation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    private String institution;
    private String degree;
    private String fieldOfStudy;
    private Integer startYear;
    private Integer endYear;
    private Boolean isCurrent;

    @Column(columnDefinition = "TEXT")
    private String description;
}
