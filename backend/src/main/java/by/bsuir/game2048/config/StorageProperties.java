package by.bsuir.game2048.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    private String root = "storage_data";
    private long maxAvatarBytes = 1_048_576;
    private long maxSaveBytes = 65_536;

    public String getRoot() { return root; }
    public void setRoot(String root) { this.root = root; }
    public long getMaxAvatarBytes() { return maxAvatarBytes; }
    public void setMaxAvatarBytes(long maxAvatarBytes) { this.maxAvatarBytes = maxAvatarBytes; }
    public long getMaxSaveBytes() { return maxSaveBytes; }
    public void setMaxSaveBytes(long maxSaveBytes) { this.maxSaveBytes = maxSaveBytes; }
}
