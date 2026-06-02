package by.bsuir.game2048.dto;

import java.time.Instant;

public class ProfileDto {

    private Long userId;
    private String username;
    private Instant createdAt;
    private boolean hasAvatar;
    private boolean hasSave;
    private String avatarUrl;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public boolean isHasAvatar() { return hasAvatar; }
    public void setHasAvatar(boolean hasAvatar) { this.hasAvatar = hasAvatar; }
    public boolean isHasSave() { return hasSave; }
    public void setHasSave(boolean hasSave) { this.hasSave = hasSave; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
