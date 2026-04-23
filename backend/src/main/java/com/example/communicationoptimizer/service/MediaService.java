package com.example.communicationoptimizer.service;

import com.example.communicationoptimizer.dto.MediaUploadRequest;
import com.example.communicationoptimizer.dto.MediaUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {

    MediaUploadResponse upload(MediaUploadRequest request);

    MediaUploadResponse uploadFile(MultipartFile file, String source, int durationMs);
}
