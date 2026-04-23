package com.example.communicationoptimizer.repository;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@ConditionalOnProperty(prefix = "app.storage", name = "mode", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryMediaStore implements MediaStore {

    private final AtomicLong mediaIdGenerator = new AtomicLong(2000);
    private final ConcurrentMap<Long, StoredMedia> mediaMap = new ConcurrentHashMap<>();

    @Override
    public StoredMedia save(Long userId, String fileName, String source, int durationMs, String fileUrl, String localFilePath) {
        long mediaId = mediaIdGenerator.incrementAndGet();

        StoredMedia storedMedia = new StoredMedia();
        storedMedia.setMediaId(mediaId);
        storedMedia.setFileName(fileName);
        storedMedia.setSource(source);
        storedMedia.setDurationMs(durationMs);
        storedMedia.setFileUrl(fileUrl);
        storedMedia.setLocalFilePath(localFilePath);

        mediaMap.put(mediaId, storedMedia);
        return storedMedia;
    }

    @Override
    public StoredMedia get(Long mediaId) {
        StoredMedia storedMedia = mediaMap.get(mediaId);
        if (storedMedia == null) {
            throw new NoSuchElementException("media not found");
        }
        return storedMedia;
    }
}
