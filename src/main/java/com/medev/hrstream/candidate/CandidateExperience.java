package com.medev.hrstream.candidate;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "candidate_experience")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Candidate candidate;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String title;

    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    @Column(name = "is_current")
    @Builder.Default
    private boolean current = false;
    @Column(columnDefinition = "TEXT")
    private String description;
}

