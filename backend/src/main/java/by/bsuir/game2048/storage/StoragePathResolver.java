package by.bsuir.game2048.storage;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Защита от выхода за пределы корня хранилища (аналог safe_join из лабы 5).
 */
public final class StoragePathResolver {

    private StoragePathResolver() {}

    public static Path resolve(Path root, String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            throw new IllegalArgumentException("Путь не задан");
        }
        String decoded = URLDecoder.decode(relativePath, StandardCharsets.UTF_8);
        String normalized = decoded.replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.contains("..")) {
            throw new IllegalArgumentException("Недопустимый путь");
        }
        Path candidate = root.resolve(normalized).normalize();
        Path rootNorm = root.toAbsolutePath().normalize();
        if (!candidate.toAbsolutePath().normalize().startsWith(rootNorm)) {
            throw new IllegalArgumentException("Путь выходит за пределы хранилища");
        }
        return candidate;
    }
}
