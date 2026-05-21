package roomescape.global.auth.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.global.auth.entity.Role;
import roomescape.global.error.ErrorCode;
import roomescape.global.error.exception.BusinessException;

@Component
public class ManagerAuthorizationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) {
        Role role = (Role) request.getAttribute("role");
        if (role != Role.MANAGER) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);
        }

        return true;
    }

}
