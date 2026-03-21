package com.medev.hrstream.candidate.profile;

import com.medev.hrstream.candidate.Candidate;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "candidate_languages")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CandidateLanguage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    private String language;

    @Enumerated(EnumType.STRING)
    private LanguageLevel level;
}
