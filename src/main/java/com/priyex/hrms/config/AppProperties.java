package com.priyex.hrms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Typed configuration properties bound to the "app" prefix in application.yml.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private FileConfig file = new FileConfig();

    @Data
    public static class Jwt {
        private String secret;
        private long accessTokenExpirationMs = 3600000;    // 1 hour
        private long refreshTokenExpirationMs = 604800000; // 7 days
    }

    @Data
    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:5173");
    }

    @Data
    public static class FileConfig {
        private String uploadDir = "./uploads";
        private String allowedExtensions = "pdf,png,jpg,jpeg,gif,doc,docx,xls,xlsx,csv,txt,zip";
        private int maxSizeMb = 10;
    }
}
