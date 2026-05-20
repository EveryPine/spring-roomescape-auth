package roomescape.global.auth.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.auth.JwtProvider;
import roomescape.global.auth.PasswordEncoder;
import roomescape.global.auth.dto.request.LoginRequestDto;
import roomescape.global.auth.dto.request.MemberCreateRequestDto;
import roomescape.global.auth.entity.Member;
import roomescape.global.auth.entity.Role;
import roomescape.global.auth.entity.TokenBlacklist;
import roomescape.global.auth.repository.MemberRepository;
import roomescape.global.auth.repository.TokenBlacklistRepository;
import roomescape.global.error.ErrorCode;
import roomescape.global.error.exception.BusinessException;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final JwtProvider jwtProvider;

    public AuthService(MemberRepository memberRepository,
        TokenBlacklistRepository tokenBlacklistRepository, JwtProvider jwtProvider) {
        this.memberRepository = memberRepository;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.jwtProvider = jwtProvider;
    }

    @Transactional
    public Member saveMember(MemberCreateRequestDto request, Role role) {
        if (memberRepository.findByLoginId(request.loginId()).isPresent()) {
            throw new BusinessException(ErrorCode.MEMBER_DUPLICATE);
        }
        String encodedPassword = PasswordEncoder.encode(request.password());
        Member member = Member.create(request.name(), request.loginId(), encodedPassword, role);

        return memberRepository.save(member);
    }

    public String login(LoginRequestDto request) {
        Optional<Member> member = memberRepository.findByLoginId(request.loginId());
        if (member.isEmpty() || !PasswordEncoder.matches(request.password(),
            member.get().getPassword())) {
            throw new BusinessException(ErrorCode.AUTH_LOGIN_FAILED);
        }

        return jwtProvider.generateToken(member.get());
    }

    @Transactional
    public void logout(String accessToken) {
        TokenBlacklist tokenBlacklist = TokenBlacklist.create(accessToken,
            jwtProvider.extractExpirationTime(accessToken));

        tokenBlacklistRepository.save(tokenBlacklist);
    }
}
