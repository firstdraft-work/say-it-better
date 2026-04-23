package com.example.communicationoptimizer.adapter.tts;

public interface TtsProvider {

    String getCode();

    String synthesize(String text);
}
