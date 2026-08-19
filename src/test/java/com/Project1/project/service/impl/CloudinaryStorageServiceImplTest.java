package com.Project1.project.service.impl;

import com.Project1.project.exception.FileUploadException;
import com.Project1.project.exception.InvalidFileException;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryStorageServiceImplTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    private CloudinaryStorageServiceImpl storageService;

    @BeforeEach
    void setUp() {
        storageService = new CloudinaryStorageServiceImpl(cloudinary);
    }

    @Test
    void uploadImage_success_returnsSecureUrl() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "laptop.png", "image/png", "sample-image-data".getBytes()
        );
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(
                Map.of("secure_url", "https://res.cloudinary.com/demo/image/upload/v123/products/laptop.png")
        );

        String resultUrl = storageService.uploadImage(file, "products");

        assertEquals("https://res.cloudinary.com/demo/image/upload/v123/products/laptop.png", resultUrl);
        verify(uploader).upload(any(byte[].class), anyMap());
    }

    @Test
    void uploadImage_emptyFile_throwsInvalidFileException() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.png", "image/png", new byte[0]
        );

        assertThrows(InvalidFileException.class, () -> storageService.uploadImage(emptyFile, "products"));
    }

    @Test
    void uploadImage_invalidMimeType_throwsInvalidFileException() {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "pdf-data".getBytes()
        );

        assertThrows(InvalidFileException.class, () -> storageService.uploadImage(pdfFile, "products"));
    }

    @Test
    void uploadImage_cloudinaryError_throwsFileUploadException() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "laptop.png", "image/png", "sample-image-data".getBytes()
        );
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new IOException("Network timeout"));

        assertThrows(FileUploadException.class, () -> storageService.uploadImage(file, "products"));
    }
}
