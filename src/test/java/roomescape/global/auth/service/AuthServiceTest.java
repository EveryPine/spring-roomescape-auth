package roomescape.global.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.global.auth.PasswordEncoder;
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
        this.authService = new AuthService(memberRepository);
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
}
