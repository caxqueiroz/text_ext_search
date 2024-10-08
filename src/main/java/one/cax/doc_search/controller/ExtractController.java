package one.cax.doc_search.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import one.cax.doc_search.exception.FileServiceException;
import one.cax.doc_search.service.FileService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * ExtractController is responsible for handling file upload and text extraction operations.
 * 
 * This controller provides endpoints for uploading files and extracting text content from them.
 * It supports both in-memory processing and temporary file storage based on configuration.
 * 
 * Key functionalities:
 * - Uploading files (currently supporting PDF format)
 * - Extracting text content from uploaded files
 * - Handling file processing errors
 * 
 * The controller uses FileService for text extraction and supports configurable
 * temporary file storage and in-memory processing options.
 */
@RestController
@RequestMapping("/extract")
@Tag(name = "ExtractController", description = "Controller for extracting text from documents")
public class ExtractController {

    private final Logger logger = Logger.getLogger(ExtractController.class.getName());
    /* The FileService - extracts text from PDFs */
    private final FileService fileService;
    /* The temporary folder to store the uploaded file */
    @Value("${doc_ext_search.temp_folder}")
    private String tempFolder;
    /* Whether to process the file in memory */
    @Value("${doc_ext_search.file_inmem_processing}")
    private boolean fileInMemProcessing;


    @Autowired
    public ExtractController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * Extract text from a file
     *
     * @param file - the file to extract text from
     * @return the extracted text
     */
    @PostMapping("/upload")
    public ResponseEntity<Object> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return new ResponseEntity<>("Please upload a file!", HttpStatus.BAD_REQUEST);
            }

            if (fileInMemProcessing) {
                byte[] bytes = file.getBytes();
                String fileType = file.getContentType();
                var xDoc = fileService.extractTextFrom(bytes, fileType);
                JSONObject response = xDoc.toJSON();
                return new ResponseEntity<>(response.toString(), HttpStatus.OK);
            }

            String fileName = UUID.randomUUID().toString() + ".pdf";
            // save file temp folder
            String tempFilePath = tempFolder + fileName;
            file.transferTo(new File(tempFilePath));
            var xDoc = fileService.extractTextFrom(tempFilePath);
            JSONObject response = xDoc.toJSON();
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);

        } catch (FileServiceException e) {
            logger.severe(e.getMessage());
            return new ResponseEntity<>("Could not upload the file: " + file.getOriginalFilename() + "!", HttpStatus.EXPECTATION_FAILED);
        } catch (Exception e) {
            logger.severe(e.getMessage());
            return new ResponseEntity<>("An error occurred while processing the file: " + file.getOriginalFilename() + "!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}