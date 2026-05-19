package roomescape.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.global.auth.AdminAuthorizationInterceptor;
import roomescape.global.auth.AuthenticationInterceptor;

@Configuration
public class AuthenticationPrincipalConfig implements WebMvcConfigurer {

    private final AuthenticationInterceptor authenticationInterceptor;
    private final AdminAuthorizationInterceptor adminAuthorizationInterceptor;

    public AuthenticationPrincipalConfig(AuthenticationInterceptor authenticationInterceptor,
        AdminAuthorizationInterceptor adminAuthorizationInterceptor) {
        this.authenticationInterceptor = authenticationInterceptor;
        this.adminAuthorizationInterceptor = adminAuthorizationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
            .addPathPatterns("/api/admin/**", "/api/reservations/**");
        registry.addInterceptor(adminAuthorizationInterceptor)
            .addPathPatterns("/api/admin/**");
    }
}
