package com.medev.hrstream.candidate.profile;

import com.medev.hrstream.candidate.Candidate;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "candidate_experience")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CandidateExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    private String company;
    private String title;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrent;

    @Column(columnDefinition = "TEXT")
    private String description;
}
