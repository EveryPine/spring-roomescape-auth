package roomescape.global.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.security.SecureRandom;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import roomescape.global.auth.entity.Member;
import roomescape.global.auth.entity.Role;
import roomescape.global.auth.interceptor.AuthenticationInterceptor;
import roomescape.global.auth.repository.FakeTokenBlacklistRepository;
import roomescape.global.error.ErrorCode;
import roomescape.global.error.exception.BusinessException;

class AuthenticationInterceptorTest {

    private final JwtProvider jwtProvider = new JwtProvider(
        secretKey(),
        3_600_000L
    );
    private final AuthenticationInterceptor interceptor = new AuthenticationInterceptor(
        new FakeTokenBlacklistRepository(),
        new AuthenticationExtractor(),
        jwtProvider
    );

    @Test
    @DisplayName("유효한 토큰이면 role을 request attribute에 저장하고 통과한다.")
    void 성공() {
        // given
        Member member = Member.create("관리자", "admin", "password", Role.ADMIN)
            .withId(1L);
        String token = jwtProvider.generateToken(member);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        // when
        boolean actual = interceptor.preHandle(request, new MockHttpServletResponse(),
            new Object());

        // then
        assertAll(
            () -> assertThat(actual).isTrue(),
            () -> assertThat(request.getAttribute("role")).isEqualTo(Role.ADMIN)
        );
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 인증 예외가 발생한다.")
    void 실패1() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();

        // when & then
        assertThatThrownBy(() ->
            interceptor.preHandle(request, new MockHttpServletResponse(), new Object())
        )
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.AUTH_UNAUTHORIZED);
    }

    @Test
    @DisplayName("유효하지 않은 토큰이면 인증 예외가 발생한다.")
    void 실패2() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");

        // when & then
        assertThatThrownBy(() ->
            interceptor.preHandle(request, new MockHttpServletResponse(), new Object())
        )
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.AUTH_INVALID_TOKEN);
    }

    private static String secretKey() {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);

        return Base64.getEncoder().encodeToString(key);
    }
}
