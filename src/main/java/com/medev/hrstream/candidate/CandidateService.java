package com.medev.hrstream.candidate;

import com.medev.hrstream.file.ResumeStorageService;
import com.medev.hrstream.jobapplication.JobApplication;
import com.medev.hrstream.jobapplication.JobApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final JobApplicationService jobApplicationService;
    private final ResumeStorageService resumeStorageService;

    public CandidateService(CandidateRepository candidateRepository, JobApplicationService jobApplicationService, ResumeStorageService resumeStorageService) {
        this.candidateRepository = candidateRepository;
        this.jobApplicationService = jobApplicationService;
        this.resumeStorageService = resumeStorageService;
    }

    public Candidate save(Candidate candidate) {
        return candidateRepository.save(candidate);
    }

    public JobApplication apply(String token, Candidate candidateData) {
        return jobApplicationService.applyByToken(token, candidateData);
    }

    public CandidateApplyResponse applyWithResume(String token, Candidate candidateData, MultipartFile resume) {
        // 1) Save/update candidate (without creating application yet)
        Candidate candidate = candidateRepository.findByEmail(candidateData.getEmail())
                .map(existing -> {
                    existing.setFirstName(candidateData.getFirstName());
                    existing.setLastName(candidateData.getLastName());
                    existing.setPhone(candidateData.getPhone());
                    existing.setNiveauEtude(candidateData.getNiveauEtude());
                    existing.setDomaineExpertise(candidateData.getDomaineExpertise());
                    existing.setExperienceProfessionnelle(candidateData.getExperienceProfessionnelle());
                    return candidateRepository.save(existing);
                })
                .orElseGet(() -> candidateRepository.save(candidateData));

        // 2) Upload resume and store metadata on candidate
        ResumeStorageService.StoredObject stored = resumeStorageService.uploadCandidateCv(candidate.getId(), resume);
        candidate.setResumeObjectKey(stored.objectKey());
        candidate.setResumeUrl(stored.url());
        candidate.setResumeOriginalName(stored.originalName());
        candidate.setResumeContentType(stored.contentType());
        candidate.setResumeSizeBytes(stored.sizeBytes());
        candidate = candidateRepository.save(candidate);

        // 3) Apply to job (creates JobApplication linking job + candidate)
        JobApplication application = jobApplicationService.applyByToken(token, candidate);

        return CandidateApplyResponse.builder()
                .candidate(candidate)
                .resume(stored)
                .application(application)
                .build();
    }

    // ── CV-specific operations ───────────────────────────────────────

    /**
     * Upload a CV for a candidate. Replaces any existing CV.
     */
    public CvResponse uploadCv(String candidateId, MultipartFile file) {
        Candidate candidate = findById(candidateId);

        // If there's an existing CV, delete it from MinIO first
        if (candidate.getResumeObjectKey() != null) {
            try {
                resumeStorageService.deleteCv(candidate.getResumeObjectKey());
            } catch (Exception e) {
                // Log but don't fail — the old file might already be gone
            }
        }

        ResumeStorageService.StoredObject stored = resumeStorageService.uploadCandidateCv(candidateId, file);

        candidate.setResumeObjectKey(stored.objectKey());
        candidate.setResumeUrl(null); // Don't cache presigned URL — always generate fresh
        candidate.setResumeOriginalName(stored.originalName());
        candidate.setResumeContentType(stored.contentType());
        candidate.setResumeSizeBytes(stored.sizeBytes());
        candidateRepository.save(candidate);

        return new CvResponse(
                stored.objectKey(),
                stored.originalName(),
                stored.contentType(),
                stored.sizeBytes(),
                stored.url()
        );
    }

    /**
     * Get a fresh presigned URL for the candidate's active CV.
     */
    public CvResponse getCv(String candidateId) {
        Candidate candidate = findById(candidateId);

        if (candidate.getResumeObjectKey() == null) {
            throw new RuntimeException("No CV found for candidate: " + candidateId);
        }

        // Always generate a fresh presigned URL (valid for 1 hour)
        String freshUrl = resumeStorageService.getCvViewUrl(candidate.getResumeObjectKey());

        return new CvResponse(
                candidate.getResumeObjectKey(),
                candidate.getResumeOriginalName(),
                candidate.getResumeContentType(),
                candidate.getResumeSizeBytes(),
                freshUrl
        );
    }

    /**
     * Delete the candidate's CV from MinIO and clear the DB reference.
     */
    public void deleteCv(String candidateId) {
        Candidate candidate = findById(candidateId);

        if (candidate.getResumeObjectKey() == null) {
            throw new RuntimeException("No CV found for candidate: " + candidateId);
        }

        // Delete from MinIO
        resumeStorageService.deleteCv(candidate.getResumeObjectKey());

        // Clear resume fields (mark as inactive)
        candidate.setResumeObjectKey(null);
        candidate.setResumeUrl(null);
        candidate.setResumeOriginalName(null);
        candidate.setResumeContentType(null);
        candidate.setResumeSizeBytes(null);
        candidateRepository.save(candidate);
    }

    // ── Existing CRUD operations ─────────────────────────────────────

    public Page<Candidate> findAll(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return candidateRepository.findAll(pageable);
    }

    public Candidate findById(String id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found with id: " + id));
    }

    public Candidate update(String id, Candidate updatedCandidate) {
        Candidate existingCandidate = findById(id);

        existingCandidate.setFirstName(updatedCandidate.getFirstName());
        existingCandidate.setLastName(updatedCandidate.getLastName());
        existingCandidate.setEmail(updatedCandidate.getEmail());
        existingCandidate.setPhone(updatedCandidate.getPhone());
        existingCandidate.setNiveauEtude(updatedCandidate.getNiveauEtude());
        existingCandidate.setDomaineExpertise(updatedCandidate.getDomaineExpertise());
        existingCandidate.setExperienceProfessionnelle(updatedCandidate.getExperienceProfessionnelle());

        return candidateRepository.save(existingCandidate);
    }

    public void delete(String id) {
        if (!candidateRepository.existsById(id)) {
            throw new RuntimeException("Candidate not found with id: " + id);
        }
        candidateRepository.deleteById(id);
    }

    /**
     * @deprecated Use {@link #getCv(String)} instead for fresh presigned URLs.
     */
    @Deprecated
    public String getResumeUrl(String candidateId) {
        Candidate candidate = findById(candidateId);
        if (candidate.getResumeObjectKey() == null) {
            throw new RuntimeException("No resume found for candidate: " + candidateId);
        }
        return resumeStorageService.getCvViewUrl(candidate.getResumeObjectKey());
    }
}
