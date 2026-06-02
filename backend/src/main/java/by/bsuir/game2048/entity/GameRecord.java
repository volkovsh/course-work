package by.bsuir.game2048.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "game_records", indexes = {
    @Index(name = "idx_game_records_score", columnList = "score"),
    @Index(name = "idx_game_records_user_id", columnList = "user_id")
})
public class GameRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false)
    private Integer maxTile;

    @Column(name = "played_at", nullable = false)
    private Instant playedAt;

    @Column(name = "moves_count")
    private Integer movesCount;

    @PrePersist
    protected void onCreate() {
        if (playedAt == null) {
            playedAt = Instant.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getMaxTile() { return maxTile; }
    public void setMaxTile(Integer maxTile) { this.maxTile = maxTile; }
    public Instant getPlayedAt() { return playedAt; }
    public void setPlayedAt(Instant playedAt) { this.playedAt = playedAt; }
    public Integer getMovesCount() { return movesCount; }
    public void setMovesCount(Integer movesCount) { this.movesCount = movesCount; }
}
