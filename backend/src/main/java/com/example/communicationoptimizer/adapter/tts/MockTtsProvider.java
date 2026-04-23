package com.example.communicationoptimizer.adapter.tts;

import org.springframework.stereotype.Component;

@Component
public class MockTtsProvider implements TtsProvider {

    @Override
    public String getCode() {
        return "mock";
    }

    @Override
    public String synthesize(String text) {
        return "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3";
    }
}
