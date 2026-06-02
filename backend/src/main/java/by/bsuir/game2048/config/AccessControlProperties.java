package by.bsuir.game2048.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app.access-control")
public class AccessControlProperties {

    private boolean enabled = true;
    private String configPath = "classpath:security-access.json";
    private int authRateLimitPerMinute = 30;
    private List<String> blockedIps = new ArrayList<>();
    private List<String> blockedPaths = new ArrayList<>();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getConfigPath() { return configPath; }
    public void setConfigPath(String configPath) { this.configPath = configPath; }
    public int getAuthRateLimitPerMinute() { return authRateLimitPerMinute; }
    public void setAuthRateLimitPerMinute(int authRateLimitPerMinute) {
        this.authRateLimitPerMinute = authRateLimitPerMinute;
    }
    public List<String> getBlockedIps() { return blockedIps; }
    public void setBlockedIps(List<String> blockedIps) { this.blockedIps = blockedIps; }
    public List<String> getBlockedPaths() { return blockedPaths; }
    public void setBlockedPaths(List<String> blockedPaths) { this.blockedPaths = blockedPaths; }
}
