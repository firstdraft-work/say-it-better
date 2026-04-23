package com.example.communicationoptimizer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.tts")
public class AppTtsProperties {

    private final Tencent tencent = new Tencent();

    public Tencent getTencent() {
        return tencent;
    }

    public static class Tencent {
        private String secretId;
        private String secretKey;
        private String endpoint = "tts.tencentcloudapi.com";
        private String region = "ap-shanghai";
        private int voiceType = 1001;
        private int sampleRate = 16000;
        private String codec = "mp3";
        private int speed = 0;
        private int volume = 5;

        public String getSecretId() {
            return secretId;
        }

        public void setSecretId(String secretId) {
            this.secretId = secretId;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public int getVoiceType() {
            return voiceType;
        }

        public void setVoiceType(int voiceType) {
            this.voiceType = voiceType;
        }

        public int getSampleRate() {
            return sampleRate;
        }

        public void setSampleRate(int sampleRate) {
            this.sampleRate = sampleRate;
        }

        public String getCodec() {
            return codec;
        }

        public void setCodec(String codec) {
            this.codec = codec;
        }

        public int getSpeed() {
            return speed;
        }

        public void setSpeed(int speed) {
            this.speed = speed;
        }

        public int getVolume() {
            return volume;
        }

        public void setVolume(int volume) {
            this.volume = volume;
        }
    }
}
