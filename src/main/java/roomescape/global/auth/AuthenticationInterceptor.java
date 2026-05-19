package roomescape.global.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.global.auth.entity.Role;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private final AuthenticationExtractor authenticationExtractor;
    private final JwtProvider jwtProvider;

    public AuthenticationInterceptor(AuthenticationExtractor authenticationExtractor,
        JwtProvider jwtProvider) {
        this.authenticationExtractor = authenticationExtractor;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) {
        String accessToken = authenticationExtractor.extract(request);
        Claims claims = jwtProvider.validateToken(accessToken);
        request.setAttribute("role", Role.valueOf((String) claims.get("role")));

        return true;
    }

}
