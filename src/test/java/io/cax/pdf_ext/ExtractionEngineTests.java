package io.cax.pdf_ext;

import io.cax.pdf_ext.exception.DocumentExtractionException;
import io.cax.pdf_ext.model.XDoc;
import io.cax.pdf_ext.service.ExtractorEngine;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class ExtractionEngineTests {

    private MeterRegistry meterRegistry;

    @BeforeEach
    public void setUp() {
        meterRegistry = new SimpleMeterRegistry();
    }

    @Test
    void testExtractTextFromPDF_NotNull() throws DocumentExtractionException {
        ExtractorEngine extractorEngine = new ExtractorEngine(meterRegistry);
        byte[] fileInBytes = TestUtils.createPdf("test data");
        XDoc xDoc = extractorEngine.extractTextFromPDF(fileInBytes);
        assertNotNull(xDoc.getDocTitle());
        assertNotNull(xDoc.getId());

    }

    @Test
    void testExtractTextFromPDF_Null() {
        ExtractorEngine extractorEngine = new ExtractorEngine(meterRegistry);
        byte[] fileInBytes = null;
        assertThrows(DocumentExtractionException.class, () -> extractorEngine.extractTextFromPDF(fileInBytes));
    }

    @Test
    void testExtractTextFromPDF_Empty() throws DocumentExtractionException {
        ExtractorEngine extractorEngine = new ExtractorEngine(meterRegistry);
        byte[] fileInBytes = TestUtils.createPdf("");
        XDoc xDoc = extractorEngine.extractTextFromPDF(fileInBytes);
        assertEquals(" ", xDoc.getDocTitle());
        
    }

    @Test
    void testExtractTextFromPDF_Large() throws DocumentExtractionException {
        ExtractorEngine extractorEngine = new ExtractorEngine(meterRegistry);
        byte[] fileInBytes = TestUtils.createPdf("a".repeat(1000000));
        XDoc xDoc = extractorEngine.extractTextFromPDF(fileInBytes);
        assertNotNull(xDoc.getDocTitle());
      
    }

    @Test
    void testExtractTextFromPDF_Invalid() {
        ExtractorEngine extractorEngine = new ExtractorEngine(meterRegistry);
        byte[] fileInBytes = "This is not a valid PDF".getBytes();
        assertThrows(DocumentExtractionException.class, () -> extractorEngine.extractTextFromPDF(fileInBytes));
    }   

}
