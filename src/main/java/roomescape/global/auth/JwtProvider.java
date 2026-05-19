package roomescape.global.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import roomescape.global.auth.entity.Member;
import roomescape.global.error.ErrorCode;
import roomescape.global.error.exception.BusinessException;

@PropertySource("classpath:security.properties")
@Component
public class JwtProvider {

    private final Key KEY;
    private final long EXPIRATION_TIME;

    public JwtProvider(@Value("${jwt.token.secret-key}") String secretKey,
        @Value("${jwt.token.expiration-time}") long expirationTime) {
        this.KEY = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.EXPIRATION_TIME = expirationTime;
    }

    public String generateToken(Member member) {
        Date now = new Date();
        return Jwts.builder()
            .setSubject(String.valueOf(member.getId()))
            .claim("loginId", member.getLoginId())
            .claim("role", member.getRole().name())
            .setIssuedAt(now)
            .setExpiration(getExpirationTime())
            .signWith(KEY)
            .compact();
    }

    private Date getExpirationTime() {
        Date now = new Date();
        return new Date(now.getTime() + EXPIRATION_TIME);
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.AUTH_EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN);
        }
    }

}
