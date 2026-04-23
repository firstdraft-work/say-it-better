package com.example.communicationoptimizer.adapter.tts;

import org.springframework.stereotype.Component;

@Component
public class AzureTtsProvider implements TtsProvider {

    @Override
    public String getCode() {
        return "azure";
    }

    @Override
    public String synthesize(String text) {
        throw new UnsupportedOperationException("Azure TTS provider is not wired yet");
    }
}
