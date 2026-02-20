// backend-spring/src/main/java/org/backend/domain/auth/service/AccessLogService.java
package org.backend.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import org.backend.domain.auth.entity.AccessLog;
import org.backend.domain.auth.repository.AccessLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccessLogService {

    private final AccessLogRepository accessLogRepository;

    public AccessLogService(AccessLogRepository accessLogRepository) {
        this.accessLogRepository = accessLogRepository;
    }

    /**
     * 메인 트랜잭션이 실패해도 로그는 남기기 위해 REQUIRES_NEW
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(HttpServletRequest request,
                    Long userId,
                    String email,
                    String role,
                    int statusCode) {

        AccessLog log = AccessLog.builder()
                .userId(userId)
                .email(email)
                .role(role)
                .method(request.getMethod())
                .path(request.getRequestURI())
                .statusCode(statusCode)
                .ip(extractClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .build();

        accessLogRepository.save(log);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String xrip = request.getHeader("X-Real-IP");
        if (xrip != null && !xrip.isBlank()) return xrip.trim();

        return request.getRemoteAddr();
    }
}