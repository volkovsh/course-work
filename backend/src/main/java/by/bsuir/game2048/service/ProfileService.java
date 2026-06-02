package by.bsuir.game2048.service;

import by.bsuir.game2048.dto.ProfileDto;
import by.bsuir.game2048.entity.User;
import by.bsuir.game2048.repository.UserRepository;
import by.bsuir.game2048.security.UserPrincipal;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ProfileDto getProfile(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        ProfileDto dto = new ProfileDto();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
