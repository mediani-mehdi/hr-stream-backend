package com.medev.hrstream.candidate.profile;

import lombok.Data;

@Data
public class UpdateBasicInfoRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String headline;
    private String summary;
    private String location;
    private String linkedinUrl;
}
