package com.example.communicationoptimizer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.providers")
public class AppProviderProperties {

    private String llm = "mock";
    private String asr = "mock";
    private String tts = "mock";

    public String getLlm() {
        return llm;
    }

    public void setLlm(String llm) {
        this.llm = llm;
    }

    public String getAsr() {
        return asr;
    }

    public void setAsr(String asr) {
        this.asr = asr;
    }

    public String getTts() {
        return tts;
    }

    public void setTts(String tts) {
        this.tts = tts;
    }
}
