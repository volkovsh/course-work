package by.bsuir.game2048.dto;

import java.time.Instant;

public class GameRecordDto {

    private Long id;
    private String username;
    private Long userId;
    private Integer score;
    private Integer maxTile;
    private Integer movesCount;
    private Instant playedAt;

    public GameRecordDto() {}

    public GameRecordDto(Long id, String username, Long userId, Integer score, Integer maxTile, Integer movesCount, Instant playedAt) {
        this.id = id;
        this.username = username;
        this.userId = userId;
        this.score = score;
        this.maxTile = maxTile;
        this.movesCount = movesCount;
        this.playedAt = playedAt;
    }

    public static GameRecordDto of(Long id, String username, Long userId, Integer score, Integer maxTile, Integer movesCount, Instant playedAt) {
        return new GameRecordDto(id, username, userId, score, maxTile, movesCount, playedAt);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getMaxTile() { return maxTile; }
    public void setMaxTile(Integer maxTile) { this.maxTile = maxTile; }
    public Integer getMovesCount() { return movesCount; }
    public void setMovesCount(Integer movesCount) { this.movesCount = movesCount; }
    public Instant getPlayedAt() { return playedAt; }
    public void setPlayedAt(Instant playedAt) { this.playedAt = playedAt; }
}
