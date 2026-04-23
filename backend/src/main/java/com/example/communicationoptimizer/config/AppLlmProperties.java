package com.example.communicationoptimizer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.llm")
public class AppLlmProperties {

    private final OpenAi openai = new OpenAi();
    private final Glm glm = new Glm();

    public OpenAi getOpenai() {
        return openai;
    }

    public Glm getGlm() {
        return glm;
    }

    public static class OpenAi {
        private String apiKey;
        private String baseUrl = "https://api.openai.com";
        private String model = "gpt-5.2";

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }
    }

    public static class Glm {
        private String apiKey;
        private String baseUrl = "https://open.bigmodel.cn";
        private String model = "glm-4.7";

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }
    }
}
