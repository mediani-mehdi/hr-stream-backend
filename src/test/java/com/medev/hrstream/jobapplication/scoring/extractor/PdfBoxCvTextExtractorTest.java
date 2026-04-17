package com.medev.hrstream.jobapplication.scoring.extractor;

import com.medev.hrstream.jobapplication.scoring.ProcessingErrorCode;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PdfBoxCvTextExtractorTest {

    private final PdfBoxCvTextExtractor extractor = new PdfBoxCvTextExtractor();

    @Test
    void extractsTextFromWellFormedPdf() {
        try (InputStream in = resource("well-formed.pdf")) {
            ExtractionResult result = extractor.extract(in);
            assertThat(result.getText()).contains("Java", "Spring Boot");
            assertThat(result.getPageCount()).isEqualTo(1);
            assertThat(result.getCharCount()).isGreaterThan(30);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void concatenatesTextAcrossPages() throws Exception {
        try (InputStream in = resource("multi-page.pdf")) {
            ExtractionResult result = extractor.extract(in);
            assertThat(result.getPageCount()).isEqualTo(3);
            assertThat(result.getText()).contains("Page 1", "Page 2", "Page 3");
        }
    }

    @Test
    void throwsCvEmptyWhenExtractedTextIsBlank() throws Exception {
        try (InputStream in = resource("empty.pdf")) {
            assertThatThrownBy(() -> extractor.extract(in))
                    .isInstanceOf(ExtractionFailedException.class)
                    .extracting(e -> ((ExtractionFailedException) e).getCode())
                    .isEqualTo(ProcessingErrorCode.CV_IMAGE_ONLY);
        }
    }

    @Test
    void throwsCvPasswordProtectedOnEncryptedPdf() throws Exception {
        try (InputStream in = resource("password-protected.pdf")) {
            assertThatThrownBy(() -> extractor.extract(in))
                    .isInstanceOf(ExtractionFailedException.class)
                    .extracting(e -> ((ExtractionFailedException) e).getCode())
                    .isEqualTo(ProcessingErrorCode.CV_PASSWORD_PROTECTED);
        }
    }

    @Test
    void throwsCvCorruptedOnInvalidBytes() {
        InputStream in = new java.io.ByteArrayInputStream("not a pdf".getBytes());
        assertThatThrownBy(() -> extractor.extract(in))
                .isInstanceOf(ExtractionFailedException.class)
                .extracting(e -> ((ExtractionFailedException) e).getCode())
                .isEqualTo(ProcessingErrorCode.CV_CORRUPTED);
    }

    private InputStream resource(String name) {
        return getClass().getClassLoader().getResourceAsStream("fixtures/cv/" + name);
    }
}
