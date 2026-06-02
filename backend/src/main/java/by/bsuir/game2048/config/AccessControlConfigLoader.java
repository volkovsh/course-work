package by.bsuir.game2048.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class AccessControlConfigLoader {

    private static final Logger log = LoggerFactory.getLogger(AccessControlConfigLoader.class);

    private final AccessControlProperties properties;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AccessControlConfigLoader(AccessControlProperties properties, ResourceLoader resourceLoader) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void loadExternalConfig() {
        String location = properties.getConfigPath();
        if (location == null || location.isBlank()) {
            return;
        }
        try {
            Resource resource = resourceLoader.getResource(location);
            if (!resource.exists()) {
                log.info("Access control config not found at {}, using application.yml defaults", location);
                return;
            }
            try (InputStream in = resource.getInputStream()) {
                JsonNode root = objectMapper.readTree(in);
                if (root.has("blocked_ips")) {
                    properties.setBlockedIps(readStringArray(root.get("blocked_ips")));
                }
                if (root.has("blocked_paths")) {
                    properties.setBlockedPaths(readStringArray(root.get("blocked_paths")));
                }
                if (root.has("auth_rate_limit_per_minute")) {
                    properties.setAuthRateLimitPerMinute(root.get("auth_rate_limit_per_minute").asInt(30));
                }
                log.info("Loaded access control: {} blocked IPs, {} blocked paths, auth limit {}/min",
                        properties.getBlockedIps().size(),
                        properties.getBlockedPaths().size(),
                        properties.getAuthRateLimitPerMinute());
            }
        } catch (Exception e) {
            log.warn("Failed to load access control config from {}: {}", location, e.getMessage());
        }
    }

    private static List<String> readStringArray(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node != null && node.isArray()) {
            node.forEach(item -> {
                if (item.isTextual()) {
                    list.add(item.asText());
                }
            });
        }
        return list;
    }
}
