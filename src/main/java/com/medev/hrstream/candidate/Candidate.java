package com.medev.hrstream.candidate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    // Resume stored in MinIO
    @Column(columnDefinition = "TEXT")
    private String resumeObjectKey;
    @Column(columnDefinition = "TEXT")
    private String resumeUrl;
    private String resumeOriginalName;
    private String resumeContentType;
    private Long resumeSizeBytes;
}
