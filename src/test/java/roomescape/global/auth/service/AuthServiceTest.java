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
import roomescape.global.auth.entity.Token;
import roomescape.global.auth.repository.FakeMemberRepository;
import roomescape.global.auth.repository.FakeTokenBlacklistRepository;
import roomescape.global.auth.repository.FakeTokenRepository;
import roomescape.global.auth.repository.MemberRepository;
import roomescape.global.auth.repository.TokenBlacklistRepository;
import roomescape.global.error.ErrorCode;
import roomescape.global.error.exception.BusinessException;

class AuthServiceTest {

    private final MemberRepository memberRepository;
    private final FakeTokenRepository tokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final JwtProvider jwtProvider;
    private final AuthService authService;

    AuthServiceTest() {
        this.memberRepository = new FakeMemberRepository();
        this.tokenRepository = new FakeTokenRepository();
        this.tokenBlacklistRepository = new FakeTokenBlacklistRepository();
        this.jwtProvider = new JwtProvider(secretKey(), 3_600_000L);
        this.authService = new AuthService(
            memberRepository,
            tokenRepository,
            tokenBlacklistRepository,
            jwtProvider
        );
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
        void 성공1() {
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
        @DisplayName("로그인에 성공하면 accessToken을 저장한다.")
        void 성공2() {
            // given
            Member member = authService.saveMember(
                new MemberCreateRequestDto("브라운", "memberId123", "password123!"),
                Role.USER
            );
            LoginRequestDto request = new LoginRequestDto("memberId123", "password123!");

            // when
            String actual = authService.login(request);

            // then
            Token savedToken = tokenRepository.findByMemberId(member.getId())
                .orElseThrow();
            assertAll(
                () -> assertThat(savedToken.getMemberId()).isEqualTo(member.getId()),
                () -> assertThat(savedToken.getToken()).isEqualTo(actual),
                () -> assertThat(savedToken.getExpiredAt())
                    .isEqualTo(jwtProvider.extractExpiration(actual))
            );
        }

        @Test
        @DisplayName("로그인에 성공하면 같은 회원의 기존 accessToken을 삭제하고 새 accessToken만 저장한다.")
        void 성공3() {
            // given
            Member member = authService.saveMember(
                new MemberCreateRequestDto("브라운", "memberId123", "password123!"),
                Role.USER
            );
            Token oldToken = Token.create(
                member.getId(),
                "old-token",
                jwtProvider.extractExpiration(jwtProvider.generateToken(member))
            );
            tokenRepository.save(oldToken);
            LoginRequestDto request = new LoginRequestDto("memberId123", "password123!");

            // when
            String actual = authService.login(request);

            // then
            assertAll(
                () -> assertThat(tokenRepository.findAllByMemberId(member.getId())).hasSize(1),
                () -> assertThat(tokenRepository.findByMemberId(member.getId()))
                    .get()
                    .extracting(Token::getToken)
                    .isEqualTo(actual)
            );
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

    @Nested
    @DisplayName("logout 테스트")
    class LogoutTest {

        @Test
        @DisplayName("유효한 토큰으로 로그아웃하면 토큰을 블랙리스트에 저장한다.")
        void 성공() {
            // given
            Member member = authService.saveMember(
                new MemberCreateRequestDto("브라운", "memberId123", "password123!"),
                Role.USER
            );
            String token = jwtProvider.generateToken(member);

            // when
            authService.logout(token);

            // then
            assertThat(tokenBlacklistRepository.existsByToken(token)).isTrue();
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 로그아웃하면 401 Unauthorized 예외가 발생한다.")
        void 실패1() {
            // given
            String invalidToken = "invalid-token";

            // when & then
            assertThatThrownBy(() -> authService.logout(invalidToken))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_INVALID_TOKEN);
            assertThat(tokenBlacklistRepository.existsByToken(invalidToken)).isFalse();
        }
    }

    private static String secretKey() {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);

        return Base64.getEncoder().encodeToString(key);
    }
}
