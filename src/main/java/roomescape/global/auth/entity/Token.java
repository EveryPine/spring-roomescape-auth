package roomescape.global.auth.entity;

import java.time.LocalDateTime;

public class Token {

    private final Long id;
    private final Long memberId;
    private final String token;
    private final LocalDateTime expiredAt;

    private Token(Long id, Long memberId, String token, LocalDateTime expiredAt) {
        this.id = id;
        this.memberId = memberId;
        this.token = token;
        this.expiredAt = expiredAt;
    }

    private Token(Long memberId, String token, LocalDateTime expiredAt) {
        this.id = null;
        this.memberId = memberId;
        this.token = token;
        this.expiredAt = expiredAt;
    }

    public static Token create(Long memberId, String token, LocalDateTime expiredAt) {
        return new Token(memberId, token, expiredAt);
    }

    public Token withId(Long id) {
        return new Token(id, this.memberId, this.token, this.expiredAt);
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }
}
