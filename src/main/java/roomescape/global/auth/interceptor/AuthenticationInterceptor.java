package roomescape.global.auth.interceptor;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.global.auth.AuthenticationExtractor;
import roomescape.global.auth.JwtProvider;
import roomescape.global.auth.entity.Role;
import roomescape.global.auth.repository.TokenRepository;
import roomescape.global.error.ErrorCode;
import roomescape.global.error.exception.BusinessException;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private final TokenRepository tokenRepository;
    private final AuthenticationExtractor authenticationExtractor;
    private final JwtProvider jwtProvider;

    public AuthenticationInterceptor(TokenRepository tokenRepository,
        AuthenticationExtractor authenticationExtractor,
        JwtProvider jwtProvider) {
        this.tokenRepository = tokenRepository;
        this.authenticationExtractor = authenticationExtractor;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) {
        String accessToken = authenticationExtractor.extract(request);
        Claims claims = jwtProvider.validateToken(accessToken);
        if (!tokenRepository.existsByToken(accessToken)) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN);
        }

        request.setAttribute("accessToken", accessToken);
        request.setAttribute("memberId", Long.valueOf(claims.getSubject()));
        request.setAttribute("role", Role.valueOf((String) claims.get("role")));

        return true;
    }

}
