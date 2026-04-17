package com.medev.hrstream.jobapplication.scoring.extractor;

import java.io.InputStream;

public interface CvTextExtractor {
    ExtractionResult extract(InputStream pdfStream);
}
