package roomescape;

import roomescape.global.auth.JwtProvider;
import roomescape.global.auth.entity.Member;
import roomescape.global.auth.entity.Role;

public class TestAuthorizationProvider {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    public TestAuthorizationProvider(final JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    public String bearerToken(Role role) {
        Member member = Member.create("테스트사용자", "login-id", "password", role)
            .withId(1L);

        return BEARER_PREFIX + jwtProvider.generateToken(member);
    }

}
