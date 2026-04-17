package com.medev.hrstream.jobapplication.scoring.extractor;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ExtractionResult {
    String text;
    int pageCount;
    int charCount;
}
