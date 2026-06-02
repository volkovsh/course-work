package by.bsuir.game2048.controller;

import by.bsuir.game2048.security.UserPrincipal;
import by.bsuir.game2048.service.FileStorageService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * REST-хранилище файлов (аватары, сохранения игры) — по мотивам лабы 5.
 */
@RestController
@RequestMapping("/api/files")
public class FileStorageController {

    private final FileStorageService storageService;

    public FileStorageController(FileStorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/avatars/{userId}")
    public ResponseEntity<Resource> getAvatar(@PathVariable Long userId) throws IOException {
        Optional<Path> avatar = storageService.findAvatar(userId);
        if (avatar.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return fileResponse(avatar.get());
    }

    @PostMapping(value = "/avatars/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadAvatar(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file) throws IOException {
        requirePrincipal(principal);
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл не выбран");
        }
        String ext = extensionFromFilename(file.getOriginalFilename());
        storageService.storeAvatar(principal.getId(), ext, file.getInputStream(), file.getSize());
        return ResponseEntity.ok(Map.of(
                "userId", principal.getId(),
                "avatarUrl", "/api/files/avatars/" + principal.getId()
        ));
    }

    @DeleteMapping("/avatars/me")
    public ResponseEntity<Void> deleteAvatar(@AuthenticationPrincipal UserPrincipal principal) throws IOException {
        requirePrincipal(principal);
        storageService.deleteAvatar(principal.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/saves/me")
    public ResponseEntity<Resource> getMySave(@AuthenticationPrincipal UserPrincipal principal) throws IOException {
        requirePrincipal(principal);
        Path path = storageService.savePath(principal.getId());
        if (!Files.isRegularFile(path)) {
            return ResponseEntity.notFound().build();
        }
        return fileResponse(path);
    }

    @PutMapping(value = "/saves/me", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> uploadSave(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody byte[] body) throws IOException {
        requirePrincipal(principal);
        storageService.storeSave(principal.getId(), new java.io.ByteArrayInputStream(body), body.length);
        return ResponseEntity.ok(Map.of("status", "saved", "savedAt", Instant.now().toString()));
    }

    @DeleteMapping("/saves/me")
    public ResponseEntity<Void> deleteSave(@AuthenticationPrincipal UserPrincipal principal) throws IOException {
        requirePrincipal(principal);
        storageService.deleteSave(principal.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/saves/me/meta")
    public ResponseEntity<Map<String, Object>> saveMeta(@AuthenticationPrincipal UserPrincipal principal) throws IOException {
        requirePrincipal(principal);
        Path path = storageService.savePath(principal.getId());
        if (!Files.isRegularFile(path)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
                "exists", true,
                "size", Files.size(path),
                "lastModified", Files.getLastModifiedTime(path).toInstant().toString()
        ));
    }

    private static ResponseEntity<Resource> fileResponse(Path path) throws IOException {
        Resource resource = new FileSystemResource(path);
        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(Files.size(path)))
                .header(HttpHeaders.LAST_MODIFIED, Files.getLastModifiedTime(path).toInstant().toString())
                .body(resource);
    }

    private static void requirePrincipal(UserPrincipal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Требуется авторизация");
        }
    }

    private static String extensionFromFilename(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "png";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
