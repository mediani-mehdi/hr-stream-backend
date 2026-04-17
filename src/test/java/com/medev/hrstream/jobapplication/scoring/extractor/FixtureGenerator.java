package com.medev.hrstream.jobapplication.scoring.extractor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

@Disabled("Run manually once to produce PDF fixtures")
class FixtureGenerator {

    private static final Path DIR = Path.of("src/test/resources/fixtures/cv");

    @Test
    void generateWellFormed() throws Exception {
        Files.createDirectories(DIR);
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.newLineAtOffset(72, 720);
                cs.showText("John Doe - Senior Java Developer");
                cs.newLineAtOffset(0, -16);
                cs.showText("Skills: Java, Spring Boot, PostgreSQL, Docker, Kubernetes, AWS");
                cs.newLineAtOffset(0, -16);
                cs.showText("Experience: 8 years building backend services");
                cs.endText();
            }
            doc.save(DIR.resolve("well-formed.pdf").toFile());
        }
    }

    @Test
    void generateEmpty() throws Exception {
        Files.createDirectories(DIR);
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(DIR.resolve("empty.pdf").toFile());
        }
    }

    @Test
    void generatePasswordProtected() throws Exception {
        Files.createDirectories(DIR);
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.newLineAtOffset(72, 720);
                cs.showText("Secret");
                cs.endText();
            }
            AccessPermission ap = new AccessPermission();
            StandardProtectionPolicy spp = new StandardProtectionPolicy("owner", "user", ap);
            spp.setEncryptionKeyLength(128);
            doc.protect(spp);
            doc.save(DIR.resolve("password-protected.pdf").toFile());
        }
    }

    @Test
    void generateImageOnly() throws Exception {
        Files.createDirectories(DIR);
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage()); // no text content stream
            doc.save(DIR.resolve("image-only.pdf").toFile());
        }
    }

    @Test
    void generateMultiPage() throws Exception {
        Files.createDirectories(DIR);
        try (PDDocument doc = new PDDocument()) {
            for (int i = 1; i <= 3; i++) {
                PDPage page = new PDPage();
                doc.addPage(page);
                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    cs.beginText();
                    cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    cs.newLineAtOffset(72, 720);
                    cs.showText("Page " + i + " content: Java Spring keywords for scoring.");
                    cs.endText();
                }
            }
            doc.save(DIR.resolve("multi-page.pdf").toFile());
        }
    }
}
