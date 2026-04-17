package com.medev.hrstream.jobapplication.scoring.extractor;

import com.medev.hrstream.jobapplication.scoring.ProcessingErrorCode;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class PdfBoxCvTextExtractor implements CvTextExtractor {

    private static final int IMAGE_ONLY_THRESHOLD_CHARS = 20;

    @Override
    public ExtractionResult extract(InputStream pdfStream) {
        byte[] bytes;
        try {
            bytes = pdfStream.readAllBytes();
        } catch (IOException e) {
            throw new ExtractionFailedException(ProcessingErrorCode.CV_CORRUPTED, "could not read PDF stream", e);
        }

        try (PDDocument doc = Loader.loadPDF(bytes)) {
            if (doc.isEncrypted()) {
                throw new ExtractionFailedException(ProcessingErrorCode.CV_PASSWORD_PROTECTED,
                        "encrypted PDF cannot be read without password");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc).trim();
            int pageCount = doc.getNumberOfPages();
            if (text.length() < IMAGE_ONLY_THRESHOLD_CHARS) {
                throw new ExtractionFailedException(ProcessingErrorCode.CV_IMAGE_ONLY,
                        "PDF contains no extractable text (likely image-only)");
            }
            return ExtractionResult.builder()
                    .text(text)
                    .pageCount(pageCount)
                    .charCount(text.length())
                    .build();
        } catch (InvalidPasswordException e) {
            throw new ExtractionFailedException(ProcessingErrorCode.CV_PASSWORD_PROTECTED,
                    "encrypted PDF", e);
        } catch (ExtractionFailedException e) {
            throw e;
        } catch (IOException e) {
            throw new ExtractionFailedException(ProcessingErrorCode.CV_CORRUPTED,
                    "could not parse PDF", e);
        }
    }
}
