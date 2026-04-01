package com.medev.hrstream.candidate;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String firstName;
    private String lastName;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    private String phone;
    private String niveauEtude;
    private String domaineExpertise;
    private String experienceProfessionnelle;
    private String headline;
    @Column(columnDefinition = "TEXT")
    private String summary;
    private String location;
    private String linkedinUrl;

    // Resume stored in MinIO
    @Column(columnDefinition = "TEXT")
    private String resumeObjectKey;
    @Column(columnDefinition = "TEXT")
    private String resumeUrl;
    private String resumeOriginalName;
    private String resumeContentType;
    private Long resumeSizeBytes;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<CandidateEducation> education = new ArrayList<>();

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<CandidateExperience> experience = new ArrayList<>();

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<CandidateSkill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<CandidateLanguage> languages = new ArrayList<>();
}
