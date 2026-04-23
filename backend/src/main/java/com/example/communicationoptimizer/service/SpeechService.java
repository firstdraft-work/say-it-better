package com.example.communicationoptimizer.service;

import com.example.communicationoptimizer.dto.AsrResponse;

public interface SpeechService {

    AsrResponse transcribe(Long userId, Long mediaId);
}
