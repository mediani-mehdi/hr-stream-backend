package com.medev.hrstream.job;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Job {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String applyLink;
    private String applicationToken;
    private String location;
    private String experienceLevel;
    private String employmentType;
    @Column(columnDefinition = "TEXT")
    private String companyDetails;
    @Column(columnDefinition = "TEXT")
    private String additionalInfo;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> skills;


    private Boolean deleted= false;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @CreationTimestamp
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime updatedDate;

    @CreatedBy
    private String createdBy;
}
