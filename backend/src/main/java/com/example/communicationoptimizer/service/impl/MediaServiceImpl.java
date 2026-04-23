package com.example.communicationoptimizer.service.impl;

import com.example.communicationoptimizer.dto.MediaUploadRequest;
import com.example.communicationoptimizer.dto.MediaUploadResponse;
import com.example.communicationoptimizer.repository.MediaStore;
import com.example.communicationoptimizer.repository.StoredMedia;
import com.example.communicationoptimizer.service.MediaService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class MediaServiceImpl implements MediaService {

    private static final Path MEDIA_DIR = Path.of("/tmp/communication-optimizer-media");

    private final MediaStore mediaStore;

    public MediaServiceImpl(MediaStore mediaStore) {
        this.mediaStore = mediaStore;
    }

    @Override
    public MediaUploadResponse upload(MediaUploadRequest request) {
        String safeSource = request.getSource() != null ? request.getSource() : "voice";
        String fileUrl = "https://example.com/mock-media/" + request.getFileName();
        StoredMedia storedMedia = mediaStore.save(
                request.getFileName(),
                safeSource,
                request.getDurationMs(),
                fileUrl,
                null
        );

        MediaUploadResponse response = new MediaUploadResponse();
        response.setMediaId(storedMedia.getMediaId());
        response.setFileUrl(storedMedia.getFileUrl());
        response.setDurationMs(storedMedia.getDurationMs());
        return response;
    }

    @Override
    public MediaUploadResponse uploadFile(MultipartFile file, String source, int durationMs) {
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "voice-input.mp3";
        String safeSource = source != null ? source : "voice";
        String localPath = saveMultipartFile(file, fileName);
        String fileUrl = "https://example.com/mock-media/" + fileName;

        StoredMedia storedMedia = mediaStore.save(
                fileName,
                safeSource,
                durationMs,
                fileUrl,
                localPath
        );

        MediaUploadResponse response = new MediaUploadResponse();
        response.setMediaId(storedMedia.getMediaId());
        response.setFileUrl(storedMedia.getFileUrl());
        response.setDurationMs(storedMedia.getDurationMs());
        return response;
    }

    private String saveMultipartFile(MultipartFile file, String fileName) {
        try {
            Files.createDirectories(MEDIA_DIR);
            Path target = MEDIA_DIR.resolve(System.currentTimeMillis() + "-" + fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toAbsolutePath().toString();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to store uploaded media file", exception);
        }
    }
}
