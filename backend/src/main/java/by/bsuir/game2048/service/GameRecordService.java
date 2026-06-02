package by.bsuir.game2048.service;

import by.bsuir.game2048.dto.GameRecordDto;
import by.bsuir.game2048.dto.GameRecordRequest;
import by.bsuir.game2048.entity.GameRecord;
import by.bsuir.game2048.entity.User;
import by.bsuir.game2048.repository.GameRecordRepository;
import by.bsuir.game2048.repository.UserRepository;
import by.bsuir.game2048.security.UserPrincipal;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GameRecordService {

    private final GameRecordRepository gameRecordRepository;
    private final UserRepository userRepository;

    public GameRecordService(GameRecordRepository gameRecordRepository, UserRepository userRepository) {
        this.gameRecordRepository = gameRecordRepository;
        this.userRepository = userRepository;
    }

    private static final int TOP_LEADERBOARD_SIZE = 100;
    private static final int USER_HISTORY_SIZE = 50;

    @Transactional
    public GameRecordDto saveRecord(UserPrincipal principal, GameRecordRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        GameRecord record = new GameRecord();
        record.setUser(user);
        record.setScore(request.getScore());
        record.setMaxTile(request.getMaxTile());
        record.setMovesCount(request.getMovesCount());
        record = gameRecordRepository.save(record);
        return toDto(record);
    }

    public List<GameRecordDto> getTopScores(int limit) {
        int size = Math.min(Math.max(limit, 1), TOP_LEADERBOARD_SIZE);
        List<GameRecord> all = gameRecordRepository.findTopScores(PageRequest.of(0, 500));
        Map<Long, GameRecord> bestPerUser = new LinkedHashMap<>();
        for (GameRecord r : all) {
            Long uid = r.getUser().getId();
            bestPerUser.putIfAbsent(uid, r);
            if (r.getScore() > bestPerUser.get(uid).getScore()) {
                bestPerUser.put(uid, r);
            }
        }
        return bestPerUser.values().stream()
                .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore()))
                .limit(size)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GameRecordDto> getMyRecords(UserPrincipal principal) {
        return gameRecordRepository.findByUserIdOrderByScoreDescWithUser(
                        principal.getId(),
                        PageRequest.of(0, USER_HISTORY_SIZE))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private GameRecordDto toDto(GameRecord r) {
        return GameRecordDto.of(
                r.getId(),
                r.getUser().getUsername(),
                r.getUser().getId(),
                r.getScore(),
                r.getMaxTile(),
                r.getMovesCount(),
                r.getPlayedAt()
        );
    }
}
