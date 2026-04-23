package com.example.communicationoptimizer.service.impl;

import com.example.communicationoptimizer.adapter.asr.AsrProvider;
import com.example.communicationoptimizer.dto.AsrResponse;
import com.example.communicationoptimizer.repository.MediaStore;
import com.example.communicationoptimizer.repository.StoredMedia;
import com.example.communicationoptimizer.service.SpeechService;
import org.springframework.stereotype.Service;

@Service
public class SpeechServiceImpl implements SpeechService {

    private final AsrProvider asrProvider;
    private final MediaStore mediaStore;

    public SpeechServiceImpl(AsrProvider asrProvider, MediaStore mediaStore) {
        this.asrProvider = asrProvider;
        this.mediaStore = mediaStore;
    }

    @Override
    public AsrResponse transcribe(Long userId, Long mediaId) {
        StoredMedia media = mediaStore.get(mediaId);

        AsrResponse response = new AsrResponse();
        response.setMediaId(mediaId);
        response.setText(asrProvider.transcribe(mediaId));
        response.setDurationMs(media.getDurationMs());
        return response;
    }
}
