package com.example.page;

import org.springframework.boot.context.properties.ConfigurationProperties;



@ConfigurationProperties(prefix = "sites")
public class UrlProperties {
    private String[] url;
    private String[] name;
    private String userAgent;

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String[] getUrl() {
        return url;
    }

    public void setUrl(String[] url) {
        this.url = url;
    }

    public String[] getName() {
        return name;
    }

    public void setName(String[] name) {
        this.name = name;
    }
}
