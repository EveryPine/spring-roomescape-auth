package roomescape;

import roomescape.global.auth.JwtProvider;
import roomescape.global.auth.entity.Member;
import roomescape.global.auth.entity.Role;
import roomescape.global.auth.entity.Token;
import roomescape.global.auth.repository.TokenRepository;

public class TestAuthorizationProvider {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final TokenRepository tokenRepository;

    public TestAuthorizationProvider(final JwtProvider jwtProvider,
        final TokenRepository tokenRepository) {
        this.jwtProvider = jwtProvider;
        this.tokenRepository = tokenRepository;
    }

    public String token(Role role, Long memberId) {
        Member member = Member.create("테스트사용자", "login-id", "password", role)
            .withId(memberId);
        String token = jwtProvider.generateToken(member);
        tokenRepository.save(
            Token.create(member.getId(), token, jwtProvider.extractExpiration(token)));

        return token;
    }

    public String bearerToken(Role role) {
        return BEARER_PREFIX + token(role, 1L);
    }

    public String bearerTokenWithMemberId(Role role, Long memberId) {
        return BEARER_PREFIX + token(role, memberId);
    }

}
