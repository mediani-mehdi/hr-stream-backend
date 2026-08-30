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
    private String department;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String location;
    private String experienceLevel;
    @Enumerated(EnumType.STRING)
    private ContractType contractType;
    @Column(columnDefinition = "TEXT")
    private String companyDetails;
    @Column(columnDefinition = "TEXT")
    private String additionalInfo;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> requiredSkills;

    private LocalDateTime dateLimte;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> niceToHaveSkills;


    private String applyLink;
    private String applicationToken;

    private Boolean deleted= false;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @Column(name = "closed_at")
    private java.time.LocalDateTime closedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "closed_reason", length = 32)
    private com.medev.hrstream.job.lifecycle.ClosedReason closedReason;

    @CreationTimestamp
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime updatedDate;

    @CreatedBy
    private String createdBy;

    @PrePersist
    public void prePersist() {
        if (this.applicationToken == null || this.applicationToken.trim().isEmpty()) {
            this.applicationToken = java.util.UUID.randomUUID().toString();
        }
        if (this.deleted == null) {
            this.deleted = false;
        }
    }
}
