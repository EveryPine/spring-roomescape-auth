package roomescape.global.auth.entity;

import java.time.LocalDateTime;

public class TokenBlacklist {

    private final Long id;
    private final String token;
    private final LocalDateTime expiredAt;

    private TokenBlacklist(Long id, String token, LocalDateTime expiredAt) {
        this.id = id;
        this.token = token;
        this.expiredAt = expiredAt;
    }

    private TokenBlacklist(String token, LocalDateTime expiredAt) {
        this.id = null;
        this.token = token;
        this.expiredAt = expiredAt;
    }

    public static TokenBlacklist create(String token, LocalDateTime expiredAt) {
        return new TokenBlacklist(token, expiredAt);
    }

    public TokenBlacklist withId(Long id) {
        return new TokenBlacklist(id, this.token, this.expiredAt);
    }

    public boolean isExpired(LocalDateTime now) {
        return expiredAt.isAfter(now);
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }
}
