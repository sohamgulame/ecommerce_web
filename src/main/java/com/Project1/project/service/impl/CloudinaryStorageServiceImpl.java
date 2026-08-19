package com.Project1.project.service.impl;

import com.Project1.project.exception.FileUploadException;
import com.Project1.project.exception.InvalidFileException;
import com.Project1.project.service.FileStorageService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryStorageServiceImpl implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryStorageServiceImpl.class);
    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "image/svg+xml"
    );

    private final Cloudinary cloudinary;

    public CloudinaryStorageServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        validateImageFile(file);

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new FileUploadException("Failed to read file bytes: " + e.getMessage(), e);
        }

        int maxRetries = 3;
        long backoffMillis = 500L;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> uploadParams = ObjectUtils.asMap(
                        "folder", folder != null && !folder.isBlank() ? folder : "products",
                        "resource_type", "image"
                );

                Map<?, ?> uploadResult = cloudinary.uploader().upload(fileBytes, uploadParams);
                Object secureUrl = uploadResult.get("secure_url");
                if (secureUrl == null) {
                    throw new FileUploadException("Cloudinary did not return a secure URL for the uploaded file.");
                }
                return secureUrl.toString();
            } catch (FileUploadException fue) {
                throw fue;
            } catch (Exception e) {
                lastException = e;
                log.warn("Cloudinary upload attempt {}/{} failed: {}", attempt, maxRetries, e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(backoffMillis * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log.error("Failed to upload image to Cloudinary after {} attempts", maxRetries, lastException);
        throw new FileUploadException("Failed to upload image to cloud storage: " + (lastException != null ? lastException.getMessage() : "Unknown error"), lastException);
    }

    @Override
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        try {
            String publicId = extractPublicId(imageUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (Exception e) {
            log.warn("Failed to delete image from Cloudinary (URL: {}): {}", imageUrl, e.getMessage());
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Cannot upload an empty file.");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidFileException("File size exceeds the maximum limit of 5MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase()) && !contentType.toLowerCase().startsWith("image/"))) {
            throw new InvalidFileException("Invalid file type. Only image files (JPEG, PNG, WebP, GIF) are allowed.");
        }
    }

    private String extractPublicId(String imageUrl) {
        try {
            // e.g. https://res.cloudinary.com/<cloud_name>/image/upload/v1234567890/products/sample.jpg
            int uploadIndex = imageUrl.indexOf("/upload/");
            if (uploadIndex == -1) {
                return null;
            }
            String pathAfterUpload = imageUrl.substring(uploadIndex + 8);
            // Skip version if present (e.g. v1234567890/)
            if (pathAfterUpload.matches("^v[0-9]+/.+")) {
                pathAfterUpload = pathAfterUpload.substring(pathAfterUpload.indexOf('/') + 1);
            }
            // Strip file extension
            int lastDotIndex = pathAfterUpload.lastIndexOf('.');
            if (lastDotIndex != -1) {
                return pathAfterUpload.substring(0, lastDotIndex);
            }
            return pathAfterUpload;
        } catch (Exception e) {
            return null;
        }
    }
}
