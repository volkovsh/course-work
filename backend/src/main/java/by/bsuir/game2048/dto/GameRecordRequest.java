package by.bsuir.game2048.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class GameRecordRequest {

    @NotNull
    @Min(0)
    private Integer score;

    @NotNull
    @Min(0)
    private Integer maxTile;

    private Integer movesCount;

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getMaxTile() { return maxTile; }
    public void setMaxTile(Integer maxTile) { this.maxTile = maxTile; }
    public Integer getMovesCount() { return movesCount; }
    public void setMovesCount(Integer movesCount) { this.movesCount = movesCount; }
}
