package com.medev.hrstream.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusRequest {
    private boolean active;

    public boolean isActive() {
        return active;
    }
}
