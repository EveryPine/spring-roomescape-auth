package roomescape.global.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import roomescape.global.error.ErrorCode;
import roomescape.global.error.exception.BusinessException;

@Component
public class AuthenticationExtractor {

    private static final String BEARER_PREFIX = "Bearer ";

    public String extract(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(ErrorCode.AUTH_UNAUTHORIZED);
        }

        return authorizationHeader.substring(BEARER_PREFIX.length());
    }
}
