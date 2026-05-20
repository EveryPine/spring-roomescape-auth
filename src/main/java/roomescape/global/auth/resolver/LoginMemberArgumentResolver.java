package roomescape.global.auth.resolver;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.global.auth.annotation.LoginMember;
import roomescape.global.auth.entity.Member;
import roomescape.global.auth.repository.MemberRepository;
import roomescape.global.error.ErrorCode;
import roomescape.global.error.exception.BusinessException;

public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final MemberRepository memberRepository;

    public LoginMemberArgumentResolver(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(LoginMember.class);
        boolean isMemberType = Member.class.isAssignableFrom(parameter.getParameterType());

        return hasAnnotation && isMemberType;
    }

    @Nullable
    @Override
    public Object resolveArgument(MethodParameter parameter,
        @Nullable ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
        @Nullable WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        Long memberId = (Long) request.getAttribute("memberId");

        return memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
