package roomescape.global.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.global.auth.entity.Role;
import roomescape.global.auth.repository.TokenBlacklistRepository;
import roomescape.global.error.ErrorCode;
import roomescape.global.error.exception.BusinessException;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final AuthenticationExtractor authenticationExtractor;
    private final JwtProvider jwtProvider;

    public AuthenticationInterceptor(TokenBlacklistRepository tokenBlacklistRepository,
        AuthenticationExtractor authenticationExtractor,
        JwtProvider jwtProvider) {
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.authenticationExtractor = authenticationExtractor;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) {
        String accessToken = authenticationExtractor.extract(request);
        Claims claims = jwtProvider.validateToken(accessToken);
        if (tokenBlacklistRepository.existsByToken(accessToken)) {
            throw new BusinessException(ErrorCode.AUTH_UNAUTHORIZED);
        }

        request.setAttribute("accessToken", accessToken);
        request.setAttribute("memberId", Long.valueOf(claims.getSubject()));
        request.setAttribute("role", Role.valueOf((String) claims.get("role")));

        return true;
    }

}
