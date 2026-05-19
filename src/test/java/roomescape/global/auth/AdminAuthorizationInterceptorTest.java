package roomescape.global.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import roomescape.global.auth.entity.Role;
import roomescape.global.error.ErrorCode;
import roomescape.global.error.exception.BusinessException;

class AdminAuthorizationInterceptorTest {

    private final AdminAuthorizationInterceptor interceptor = new AdminAuthorizationInterceptor();

    @Test
    @DisplayName("관리자 권한이면 통과한다.")
    void 성공() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("role", Role.ADMIN);

        // when
        boolean actual = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        // then
        assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("사용자 권한이면 인가 예외가 발생한다.")
    void 실패1() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("role", Role.USER);

        // when & then
        assertThatThrownBy(() ->
            interceptor.preHandle(request, new MockHttpServletResponse(), new Object())
        )
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.AUTH_FORBIDDEN);
    }

    @Test
    @DisplayName("권한 정보가 없으면 인가 예외가 발생한다.")
    void 실패2() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();

        // when & then
        assertThatThrownBy(() ->
            interceptor.preHandle(request, new MockHttpServletResponse(), new Object())
        )
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.AUTH_FORBIDDEN);
    }
}
