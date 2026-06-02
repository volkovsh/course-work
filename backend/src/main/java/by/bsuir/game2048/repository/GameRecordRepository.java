package by.bsuir.game2048.repository;

import by.bsuir.game2048.entity.GameRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRecordRepository extends JpaRepository<GameRecord, Long> {

    List<GameRecord> findByUserIdOrderByScoreDesc(Long userId, Pageable pageable);

    @Query("SELECT gr FROM GameRecord gr JOIN FETCH gr.user WHERE gr.user.id = :userId ORDER BY gr.score DESC")
    List<GameRecord> findByUserIdOrderByScoreDescWithUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT gr FROM GameRecord gr JOIN FETCH gr.user ORDER BY gr.score DESC")
    List<GameRecord> findTopScores(Pageable pageable);
}
