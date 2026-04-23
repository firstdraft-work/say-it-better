package com.example.communicationoptimizer.controller;

import com.example.communicationoptimizer.common.ApiResponse;
import com.example.communicationoptimizer.adapter.tts.LocalAudioFileStore;
import com.example.communicationoptimizer.dto.MediaUploadRequest;
import com.example.communicationoptimizer.dto.MediaUploadResponse;
import com.example.communicationoptimizer.service.MediaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

    private final MediaService mediaService;
    private final LocalAudioFileStore audioFileStore;

    public MediaController(MediaService mediaService, LocalAudioFileStore audioFileStore) {
        this.mediaService = mediaService;
        this.audioFileStore = audioFileStore;
    }

    @PostMapping("/upload")
    public ApiResponse<MediaUploadResponse> upload(@Valid @RequestBody MediaUploadRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ApiResponse.success(mediaService.upload(userId, request));
    }

    @PostMapping("/upload-file")
    public ApiResponse<MediaUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "durationMs", required = false, defaultValue = "0") int durationMs,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ApiResponse.success(mediaService.uploadFile(userId, file, source, durationMs));
    }

    @GetMapping("/audio/{fileName}")
    public ResponseEntity<Resource> getAudio(@PathVariable String fileName) {
        Path path = audioFileStore.get(fileName);
        Resource resource = new FileSystemResource(path);

        MediaType mediaType = fileName.endsWith(".wav") ? MediaType.parseMediaType("audio/wav") : MediaType.parseMediaType("audio/mpeg");

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .contentType(mediaType)
                .body(resource);
    }
}
