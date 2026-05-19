package roomescape.global.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.auth.PasswordEncoder;
import roomescape.global.auth.dto.request.MemberCreateRequestDto;
import roomescape.global.auth.entity.Member;
import roomescape.global.auth.entity.Role;
import roomescape.global.auth.repository.MemberRepository;
import roomescape.global.error.ErrorCode;
import roomescape.global.error.exception.BusinessException;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;

    public AuthService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Member saveMember(MemberCreateRequestDto request, Role role) {
        if (memberRepository.existsByLoginId(request.loginId())) {
            throw new BusinessException(ErrorCode.MEMBER_DUPLICATE);
        }
        String encodedPassword = PasswordEncoder.encode(request.password());
        Member member = Member.create(request.name(), request.loginId(), encodedPassword, role);

        return memberRepository.save(member);
    }
}
