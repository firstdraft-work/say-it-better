package com.example.communicationoptimizer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.asr")
public class AppAsrProperties {

    private final Tencent tencent = new Tencent();

    public Tencent getTencent() {
        return tencent;
    }

    public static class Tencent {
        private String secretId;
        private String secretKey;
        private String endpoint = "asr.tencentcloudapi.com";
        private String region = "ap-shanghai";
        private String engineType = "16k_zh";

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

        public String getEngineType() {
            return engineType;
        }

        public void setEngineType(String engineType) {
            this.engineType = engineType;
        }
    }
}
