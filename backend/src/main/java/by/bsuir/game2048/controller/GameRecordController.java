package by.bsuir.game2048.controller;

import by.bsuir.game2048.dto.GameRecordDto;
import by.bsuir.game2048.dto.GameRecordRequest;
import by.bsuir.game2048.security.UserPrincipal;
import by.bsuir.game2048.service.GameRecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
public class GameRecordController {

    private final GameRecordService gameRecordService;

    public GameRecordController(GameRecordService gameRecordService) {
        this.gameRecordService = gameRecordService;
    }

    @PostMapping
    public ResponseEntity<GameRecordDto> saveRecord(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody GameRecordRequest request) {
        return ResponseEntity.ok(gameRecordService.saveRecord(principal, request));
    }

    @GetMapping("/top")
    public ResponseEntity<List<GameRecordDto>> getTopScores(
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(gameRecordService.getTopScores(limit));
    }

    @GetMapping("/my")
    public ResponseEntity<List<GameRecordDto>> getMyRecords(
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(gameRecordService.getMyRecords(principal));
    }
}
