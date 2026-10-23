package com.priyex.hrms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Typed configuration properties bound to the "app" prefix in application.yml.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private FileConfig file = new FileConfig();
    private String platformUrl = "https://www.priyex.com";
    private Mail mail = new Mail();
    private Brevo brevo = new Brevo();
    private Resend resend = new Resend();
    private Whatsapp whatsapp = new Whatsapp();

    public AppProperties() {
    }

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long accessTokenExpirationMs = 3600000;    // 1 hour
        private long refreshTokenExpirationMs = 604800000; // 7 days

        public Jwt() {
        }
    }

    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:5173");

        public Cors() {
        }
    }

    @Getter
    @Setter
    public static class FileConfig {
        private String uploadDir = "./uploads";
        private String allowedExtensions = "pdf,png,jpg,jpeg,gif,doc,docx,xls,xlsx,csv,txt,zip";
        private int maxSizeMb = 10;

        public FileConfig() {
        }
    }

    @Getter
    @Setter
    public static class Mail {
        private String from = "tech.priyex@gmail.com";
        private String alertRecipients = "tech.priyex@gmail.com,in.rkumar91@gmail.com";

        public Mail() {
        }
    }

    @Getter
    @Setter
    public static class Brevo {
        private String apiKey = "";
        private String senderName = "Priyex People HR Operations";

        public Brevo() {
        }
    }

    @Getter
    @Setter
    public static class Resend {
        private String apiKey = "";
        private String fromEmail = "Priyex HRMS <onboarding@resend.dev>";

        public Resend() {
        }
    }

    @Getter
    @Setter
    public static class Whatsapp {
        private boolean enabled = true;
        private String provider = "META_CLOUD";
        private String businessPhone = "+919901694683";
        private String apiToken = "";
        private String phoneNumberId = "";
        private String apiUrl = "https://graph.facebook.com/v20.0";
        private String salesAlertPhone = "+919901694683";

        public Whatsapp() {
        }
    }
}
