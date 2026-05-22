package roomescape.global.auth.entity;

import java.time.LocalDateTime;

public class Token {

    private final Long id;
    private final String token;
    private final LocalDateTime expiredAt;

    private Token(Long id, String token, LocalDateTime expiredAt) {
        this.id = id;
        this.token = token;
        this.expiredAt = expiredAt;
    }

    private Token(String token, LocalDateTime expiredAt) {
        this.id = null;
        this.token = token;
        this.expiredAt = expiredAt;
    }

    public static Token create(String token, LocalDateTime expiredAt) {
        return new Token(token, expiredAt);
    }

    public Token withId(Long id) {
        return new Token(id, this.token, this.expiredAt);
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
