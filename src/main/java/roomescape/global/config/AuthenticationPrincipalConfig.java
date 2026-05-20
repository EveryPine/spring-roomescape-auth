package roomescape.global.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.global.auth.AdminAuthorizationInterceptor;
import roomescape.global.auth.AuthenticationInterceptor;
import roomescape.global.auth.repository.MemberRepository;
import roomescape.global.auth.resolver.AuthTokenArgumentResolver;
import roomescape.global.auth.resolver.LoginMemberArgumentResolver;

@Configuration
public class AuthenticationPrincipalConfig implements WebMvcConfigurer {

    private final AuthenticationInterceptor authenticationInterceptor;
    private final AdminAuthorizationInterceptor adminAuthorizationInterceptor;
    private final MemberRepository memberRepository;

    public AuthenticationPrincipalConfig(AuthenticationInterceptor authenticationInterceptor,
        AdminAuthorizationInterceptor adminAuthorizationInterceptor,
        MemberRepository memberRepository) {
        this.authenticationInterceptor = authenticationInterceptor;
        this.adminAuthorizationInterceptor = adminAuthorizationInterceptor;
        this.memberRepository = memberRepository;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
            .addPathPatterns("/api/auth/logout", "/api/admin/**", "/api/reservations/**");
        registry.addInterceptor(adminAuthorizationInterceptor)
            .addPathPatterns("/api/admin/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver(memberRepository));
        resolvers.add(new AuthTokenArgumentResolver());
    }
}
