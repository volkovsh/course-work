package by.bsuir.game2048.filter;

import by.bsuir.game2048.config.AccessControlProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Чёрный список IP/путей и rate limit на /api/auth/* (лаба 4 blacklist).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AccessControlFilter extends OncePerRequestFilter {

    private static final String BLOCKED_HTML = """
            <!DOCTYPE html>
            <html lang="ru"><head><meta charset="UTF-8"><title>Доступ запрещён</title></head>
            <body><h1>403 Forbidden</h1><p>Запрос заблокирован политикой доступа сервера.</p></body></html>
            """;

    private final AccessControlProperties properties;
    private final Map<String, RateBucket> authBuckets = new ConcurrentHashMap<>();

    public AccessControlFilter(AccessControlProperties properties) {
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !properties.isEnabled();
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String clientIp = ApiLoggingFilter.clientIp(request);
        String path = request.getRequestURI();

        if (isIpBlocked(clientIp)) {
            sendForbidden(request, response, "IP blocked");
            return;
        }
        if (isPathBlocked(path)) {
            sendForbidden(request, response, "Path blocked");
            return;
        }
        if (isAuthPath(path) && isRateLimited(clientIp)) {
            sendForbidden(request, response, "Too many auth requests");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isIpBlocked(String ip) {
        for (String blocked : properties.getBlockedIps()) {
            if (blocked == null || blocked.isBlank()) continue;
            if (ip.equals(blocked)) return true;
        }
        return false;
    }

    private boolean isPathBlocked(String path) {
        for (String blocked : properties.getBlockedPaths()) {
            if (blocked == null || blocked.isBlank()) continue;
            if (path.equals(blocked) || path.startsWith(blocked)) return true;
        }
        return false;
    }

    private static boolean isAuthPath(String path) {
        return "/api/auth/login".equals(path) || "/api/auth/register".equals(path);
    }

    private boolean isRateLimited(String ip) {
        int limit = Math.max(1, properties.getAuthRateLimitPerMinute());
        long windowStart = Instant.now().getEpochSecond() / 60;
        String key = ip + ":" + windowStart;
        RateBucket bucket = authBuckets.computeIfAbsent(key, k -> new RateBucket());
        if (bucket.count.incrementAndGet() > limit) {
            return true;
        }
        if (authBuckets.size() > 10_000) {
            authBuckets.clear();
        }
        return false;
    }

    private void sendForbidden(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"" + message + "\"}");
        } else {
            response.setContentType(MediaType.TEXT_HTML_VALUE + ";charset=UTF-8");
            response.getOutputStream().write(BLOCKED_HTML.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static final class RateBucket {
        final AtomicInteger count = new AtomicInteger(0);
    }
}
