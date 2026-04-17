package com.medev.hrstream.integration.support;

import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;

public final class TestPdfFixtures {

    private TestPdfFixtures() {}

    public static MockMultipartFile wellFormed(String name)        { return load("well-formed.pdf", name); }
    public static MockMultipartFile empty(String name)             { return load("empty.pdf", name); }
    public static MockMultipartFile passwordProtected(String name) { return load("password-protected.pdf", name); }
    public static MockMultipartFile imageOnly(String name)         { return load("image-only.pdf", name); }
    public static MockMultipartFile multiPage(String name)         { return load("multi-page.pdf", name); }

    private static MockMultipartFile load(String file, String fieldName) {
        try (InputStream in = TestPdfFixtures.class.getClassLoader()
                .getResourceAsStream("fixtures/cv/" + file)) {
            if (in == null) throw new IllegalStateException("missing fixture: " + file);
            return new MockMultipartFile(fieldName, file, "application/pdf", in.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
