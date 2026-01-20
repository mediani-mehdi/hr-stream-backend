package com.medev.hrstream.job;

public enum JobStatus {
    DRAFT,      // 1. Being written. Not visible to anyone.
    OPEN,       // 2. Published. Accepting applications via the link.
    FILLED,     // 3. Position successfully occupied. No more applications.
    CANCELLED,  // 4. Closed without hiring (budget cut, change of plans).
    ON_HOLD,     // 5. (Optional) Paused. Link is disabled temporarily.
    DELETED     // 6. (Optional) Removed from system. Not visible anywhere.
}
