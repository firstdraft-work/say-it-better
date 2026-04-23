package com.example.communicationoptimizer.adapter.asr;

import org.springframework.stereotype.Component;

@Component
public class OpenAiAsrProvider implements AsrProvider {

    @Override
    public String getCode() {
        return "openai";
    }

    @Override
    public String transcribe(Long mediaId) {
        throw new UnsupportedOperationException("OpenAI ASR provider is not wired yet");
    }
}
