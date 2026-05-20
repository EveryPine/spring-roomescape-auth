package roomescape.global.auth.resolver;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.global.auth.annotation.AuthToken;

public class AuthTokenArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(AuthToken.class);
        boolean isMemberType = String.class.isAssignableFrom(parameter.getParameterType());

        return hasAnnotation && isMemberType;
    }

    @Nullable
    @Override
    public Object resolveArgument(MethodParameter parameter,
        @Nullable ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
        @Nullable WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        return request.getAttribute("accessToken");
    }

}
