package com.flowershop.common.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "flower")
public class FlowerProperties {

    private Jwt jwt = new Jwt();
    private Upload upload = new Upload();
    private Cors cors = new Cors();
    private Order order = new Order();

    @Data
    public static class Jwt {

        private String issuer;
        private String secret;
        private Long expireSeconds;
    }

    @Data
    public static class Upload {

        private String baseDir;
        private String publicPrefix;
    }

    @Data
    public static class Cors {

        private List<String> allowedOrigins = new ArrayList<>();
    }

    @Data
    public static class Order {

        private Integer timeoutMinutes = 30;
    }
}
