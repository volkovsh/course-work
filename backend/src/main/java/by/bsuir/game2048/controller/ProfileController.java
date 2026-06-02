package by.bsuir.game2048.controller;

import by.bsuir.game2048.dto.ProfileDto;
import by.bsuir.game2048.security.UserPrincipal;
import by.bsuir.game2048.service.FileStorageService;
import by.bsuir.game2048.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final FileStorageService fileStorageService;

    public ProfileController(ProfileService profileService, FileStorageService fileStorageService) {
        this.profileService = profileService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ResponseEntity<ProfileDto> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        ProfileDto dto = profileService.getProfile(principal);
        dto.setHasAvatar(fileStorageService.findAvatar(principal.getId()).isPresent());
        try {
            dto.setHasSave(Files.isRegularFile(fileStorageService.savePath(principal.getId())));
        } catch (Exception e) {
            dto.setHasSave(false);
        }
        if (dto.isHasAvatar()) {
            dto.setAvatarUrl("/api/files/avatars/" + principal.getId());
        }
        return ResponseEntity.ok(dto);
    }
}
