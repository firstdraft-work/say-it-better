package com.example.communicationoptimizer.repository;

public interface MediaStore {

    StoredMedia save(Long userId, String fileName, String source, int durationMs, String fileUrl, String localFilePath);

    StoredMedia get(Long mediaId);
}
