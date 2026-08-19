package com.Project1.project.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Validates and uploads an image to cloud storage.
     *
     * @param file   the multipart file to upload
     * @param folder the target folder name (e.g. "products")
     * @return the secure public URL of the uploaded image
     */
    String uploadImage(MultipartFile file, String folder);

    /**
     * Deletes an image from cloud storage if public ID can be resolved.
     *
     * @param imageUrl the URL of the image to delete
     */
    void deleteImage(String imageUrl);
}
