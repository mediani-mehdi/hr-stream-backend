package com.medev.hrstream.candidate.profile.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateBasicInfoRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Email
    private String email;

    @Size(max = 30)
    private String phone;

    @Size(max = 255)
    private String headline;

    private String summary;
    private String location;
    private String linkedinUrl;
    private String niveauEtude;
    private String domaineExpertise;
    private String experienceProfessionnelle;
}

