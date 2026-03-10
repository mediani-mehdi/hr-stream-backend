package com.medev.hrstream.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordResponse {
    /**
     * Always true if request was accepted (even if user doesn't exist), to prevent user enumeration.
     */
    private boolean accepted;

    /**
     * For dev/testing you may return the reset token. In production, keep this null.
     */
    private String resetToken;
}

