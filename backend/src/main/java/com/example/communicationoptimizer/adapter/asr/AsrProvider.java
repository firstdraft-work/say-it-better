package com.example.communicationoptimizer.adapter.asr;

public interface AsrProvider {

    String getCode();

    String transcribe(Long mediaId);
}
