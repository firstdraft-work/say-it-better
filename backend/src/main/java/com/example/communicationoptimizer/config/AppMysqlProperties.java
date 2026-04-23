package com.example.communicationoptimizer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.mysql")
public class AppMysqlProperties {

    private String url;
    private String username;
    private String password;
    private String driverClassName = "com.mysql.cj.jdbc.Driver";
    private boolean autoInitSchema = false;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public boolean isAutoInitSchema() {
        return autoInitSchema;
    }

    public void setAutoInitSchema(boolean autoInitSchema) {
        this.autoInitSchema = autoInitSchema;
    }
}
