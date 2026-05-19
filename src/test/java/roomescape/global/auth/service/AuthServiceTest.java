package roomescape.global.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.security.SecureRandom;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.global.auth.JwtProvider;
import roomescape.global.auth.PasswordEncoder;
import roomescape.global.auth.dto.request.LoginRequestDto;
import roomescape.global.auth.dto.request.MemberCreateRequestDto;
import roomescape.global.auth.entity.Member;
import roomescape.global.auth.entity.Role;
import roomescape.global.auth.repository.FakeMemberRepository;
import roomescape.global.auth.repository.MemberRepository;
import roomescape.global.error.ErrorCode;
import roomescape.global.error.exception.BusinessException;

class AuthServiceTest {

    private final MemberRepository memberRepository;
    private final AuthService authService;

    AuthServiceTest() {
        this.memberRepository = new FakeMemberRepository();
        JwtProvider jwtProvider = new JwtProvider(secretKey(), 3_600_000L);
        this.authService = new AuthService(memberRepository, jwtProvider);
    }

    @Nested
    @DisplayName("saveMember 테스트")
    class SaveMemberTest {

        @Test
        @DisplayName("회원가입을 하면 비밀번호를 암호화하고 회원을 저장한다.")
        void 성공() {
            // given
            MemberCreateRequestDto request = new MemberCreateRequestDto(
                "브라운",
                "memberId123",
                "password123!"
            );

            // when
            Member actual = authService.saveMember(request, Role.USER);

            // then
            assertAll(
                () -> assertThat(actual.getId()).isEqualTo(1L),
                () -> assertThat(actual.getName()).isEqualTo("브라운"),
                () -> assertThat(actual.getLoginId()).isEqualTo("memberId123"),
                () -> assertThat(actual.getRole()).isEqualTo(Role.USER),
                () -> assertThat(actual.getPassword()).isNotEqualTo("password123!"),
                () -> assertThat(PasswordEncoder.matches("password123!", actual.getPassword()))
                    .isTrue()
            );
        }

        @Test
        @DisplayName("같은 memberId를 가진 회원이 존재하면 예외가 발생한다.")
        void 실패1() {
            // given
            authService.saveMember(
                new MemberCreateRequestDto("브라운", "memberId123", "password123!"),
                Role.USER
            );
            MemberCreateRequestDto request = new MemberCreateRequestDto(
                "네오",
                "memberId123",
                "anotherPassword123!"
            );

            // when & then
            assertThatThrownBy(() -> authService.saveMember(request, Role.USER))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MEMBER_DUPLICATE);
        }
    }

    @Nested
    @DisplayName("login 테스트")
    class LoginTest {

        @Test
        @DisplayName("올바른 아이디와 비밀번호로 로그인하면 accessToken을 반환한다.")
        void 성공() {
            // given
            authService.saveMember(
                new MemberCreateRequestDto("브라운", "memberId123", "password123!"),
                Role.USER
            );
            LoginRequestDto request = new LoginRequestDto("memberId123", "password123!");

            // when
            String actual = authService.login(request);

            // then
            assertThat(actual).isNotBlank();
        }

        @Test
        @DisplayName("존재하지 않는 아이디로 로그인하면 401 Unauthorized 예외가 발생한다.")
        void 실패1() {
            // given
            LoginRequestDto request = new LoginRequestDto("unknownMemberId", "password123!");

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_LOGIN_FAILED);
        }

        @Test
        @DisplayName("비밀번호가 틀리면 401 Unauthorized 예외가 발생한다.")
        void 실패2() {
            // given
            authService.saveMember(
                new MemberCreateRequestDto("브라운", "memberId123", "password123!"),
                Role.USER
            );
            LoginRequestDto request = new LoginRequestDto("memberId123", "wrongPassword");

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_LOGIN_FAILED);
        }
    }

    private static String secretKey() {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);

        return Base64.getEncoder().encodeToString(key);
    }
}
