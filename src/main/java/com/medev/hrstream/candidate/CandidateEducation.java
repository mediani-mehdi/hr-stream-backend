package com.medev.hrstream.candidate;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "candidate_education")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateEducation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Candidate candidate;

    @Column(nullable = false)
    private String institution;

    private String degree;
    private String fieldOfStudy;
    private Integer startYear;
    private Integer endYear;
    @Column(name = "is_current")
    @Builder.Default
    private boolean current = false;
    @Column(columnDefinition = "TEXT")
    private String description;
}

