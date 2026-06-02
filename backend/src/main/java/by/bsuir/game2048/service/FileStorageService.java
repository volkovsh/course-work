package by.bsuir.game2048.service;

import by.bsuir.game2048.config.StorageProperties;
import by.bsuir.game2048.storage.StoragePathResolver;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.Set;

@Service
public class FileStorageService {

    private static final Set<String> AVATAR_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp", "gif");

    private final StorageProperties properties;
    private Path root;

    public FileStorageService(StorageProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void init() throws IOException {
        root = Path.of(properties.getRoot()).toAbsolutePath().normalize();
        Files.createDirectories(root.resolve("avatars"));
        Files.createDirectories(root.resolve("saves"));
    }

    public Path getRoot() {
        return root;
    }

    public Path resolvePublic(String relativePath) {
        return StoragePathResolver.resolve(root, relativePath);
    }

    public Path avatarPath(Long userId, String extension) {
        String ext = normalizeAvatarExtension(extension);
        return resolvePublic("avatars/" + userId + "." + ext);
    }

    public Path savePath(Long userId) {
        return resolvePublic("saves/" + userId + ".json");
    }

    public Optional<Path> findAvatar(Long userId) {
        for (String ext : AVATAR_EXTENSIONS) {
            Path path = root.resolve("avatars").resolve(userId + "." + ext);
            if (Files.isRegularFile(path)) {
                return Optional.of(path);
            }
        }
        return Optional.empty();
    }

    public void storeAvatar(Long userId, String extension, InputStream data, long size) throws IOException {
        if (size > properties.getMaxAvatarBytes()) {
            throw new IllegalArgumentException("Файл аватара слишком большой");
        }
        deleteAvatarFiles(userId);
        Path target = avatarPath(userId, extension);
        Files.createDirectories(target.getParent());
        Files.copy(data, target, StandardCopyOption.REPLACE_EXISTING);
    }

    public void storeSave(Long userId, InputStream data, long size) throws IOException {
        if (size > properties.getMaxSaveBytes()) {
            throw new IllegalArgumentException("Файл сохранения слишком большой");
        }
        Path target = savePath(userId);
        Files.createDirectories(target.getParent());
        Path temp = Files.createTempFile(target.getParent(), "save-", ".tmp");
        try {
            Files.copy(data, temp, StandardCopyOption.REPLACE_EXISTING);
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    public void deleteSave(Long userId) throws IOException {
        Path path = savePath(userId);
        Files.deleteIfExists(path);
    }

    public void deleteAvatar(Long userId) throws IOException {
        deleteAvatarFiles(userId);
    }

    private void deleteAvatarFiles(Long userId) throws IOException {
        for (String ext : AVATAR_EXTENSIONS) {
            Files.deleteIfExists(root.resolve("avatars").resolve(userId + "." + ext));
        }
    }

    private static String normalizeAvatarExtension(String extension) {
        String ext = extension == null ? "png" : extension.toLowerCase().replace(".", "");
        if ("jpeg".equals(ext)) {
            ext = "jpg";
        }
        if (!AVATAR_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("Неподдерживаемый формат изображения");
        }
        return ext;
    }
}
