package roomescape.global.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.auth.JwtProvider;
import roomescape.global.auth.PasswordEncoder;
import roomescape.global.auth.dto.request.LoginRequestDto;
import roomescape.global.auth.dto.request.MemberCreateRequestDto;
import roomescape.global.auth.entity.Member;
import roomescape.global.auth.entity.Role;
import roomescape.global.auth.entity.Token;
import roomescape.global.auth.repository.MemberRepository;
import roomescape.global.auth.repository.TokenRepository;
import roomescape.global.error.ErrorCode;
import roomescape.global.error.exception.BusinessException;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final TokenRepository tokenRepository;
    private final JwtProvider jwtProvider;

    public AuthService(MemberRepository memberRepository, TokenRepository tokenRepository,
        JwtProvider jwtProvider) {
        this.memberRepository = memberRepository;
        this.tokenRepository = tokenRepository;
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

    @Transactional
    public String login(LoginRequestDto request) {
        Member member = memberRepository.findByLoginId(request.loginId())
            .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_LOGIN_FAILED));
        if (!PasswordEncoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(ErrorCode.AUTH_LOGIN_FAILED);
        }

        String token = jwtProvider.generateToken(member);
        tokenRepository.deleteByMemberId(member.getId());
        tokenRepository.save(
            Token.create(member.getId(), token, jwtProvider.extractExpiration(token)));

        return token;
    }

    @Transactional
    public void logout(Long memberId) {
        tokenRepository.deleteByMemberId(memberId);
    }
}
