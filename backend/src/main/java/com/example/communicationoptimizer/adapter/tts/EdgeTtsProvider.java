package com.example.communicationoptimizer.adapter.tts;

import org.springframework.stereotype.Component;

@Component
public class EdgeTtsProvider implements TtsProvider {

    @Override
    public String getCode() {
        return "edge";
    }

    @Override
    public String synthesize(String text) {
        throw new UnsupportedOperationException("Edge TTS provider is not wired yet");
    }
}
