package com.example.communicationoptimizer.adapter.asr;

import org.springframework.stereotype.Component;

@Component
public class LocalAsrProvider implements AsrProvider {

    @Override
    public String getCode() {
        return "local";
    }

    @Override
    public String transcribe(Long mediaId) {
        throw new UnsupportedOperationException("Local ASR provider is not wired yet");
    }
}
