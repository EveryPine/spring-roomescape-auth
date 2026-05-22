package roomescape.global.config;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import java.security.SecureRandom;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import roomescape.global.auth.JwtProvider;
import roomescape.global.auth.entity.Member;
import roomescape.global.auth.entity.Role;
import roomescape.global.auth.entity.Token;
import roomescape.global.auth.repository.TokenRepository;
import roomescape.global.error.ErrorCode;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthenticationPrincipalConfigTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private TokenRepository tokenRepository;

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) {
        registry.add("jwt.token.secret-key", AuthenticationPrincipalConfigTest::secretKey);
        registry.add("jwt.token.expiration-time", () -> 3_600_000L);
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("관리자 API는 토큰이 없으면 401을 반환한다.")
    void 관리자_API_인증_실패() {
        given().when().get("/api/admin/times").then().statusCode(401)
            .body("message", equalTo(ErrorCode.AUTH_UNAUTHORIZED.getMessage()));
    }

    @Test
    @DisplayName("관리자 API는 사용자 토큰이면 403을 반환한다.")
    void 관리자_API_인가_실패() {
        given().header("Authorization", "Bearer " + token(Role.USER)).when().get("/api/admin/times")
            .then().statusCode(403).body("message", equalTo(ErrorCode.AUTH_FORBIDDEN.getMessage()));
    }

    @Test
    @DisplayName("관리자 API는 관리자 토큰이면 통과한다.")
    void 관리자_API_인가_성공() {
        given().header("Authorization", "Bearer " + token(Role.ADMIN)).when()
            .get("/api/admin/times").then().statusCode(200);
    }

    @Test
    @DisplayName("매니저 API는 토큰이 없으면 401을 반환한다.")
    void 매니저_API_인증_실패() {
        given().when().get("/api/manager/reservations").then().statusCode(401)
            .body("message", equalTo(ErrorCode.AUTH_UNAUTHORIZED.getMessage()));
    }

    @Test
    @DisplayName("매니저 API는 사용자 토큰이면 403을 반환한다.")
    void 매니저_API_인가_실패() {
        given().header("Authorization", "Bearer " + token(Role.USER)).when()
            .get("/api/manager/reservations")
            .then().statusCode(403).body("message", equalTo(ErrorCode.AUTH_FORBIDDEN.getMessage()));
    }

    @Test
    @DisplayName("매니저 API는 매니저 토큰이면 통과한다.")
    void 매니저_API_인가_성공() {
        given().header("Authorization", "Bearer " + token(Role.MANAGER)).when()
            .get("/api/manager/reservations").then().statusCode(200);
    }

    @Test
    @DisplayName("사용자 예약 API는 토큰이 없으면 401을 반환한다.")
    void 사용자_예약_API_인증_실패() {
        given().when().get("/api/reservations").then().statusCode(401)
            .body("message", equalTo(ErrorCode.AUTH_UNAUTHORIZED.getMessage()));
    }

    @Test
    @DisplayName("로그아웃 API는 토큰이 없으면 401을 반환한다.")
    void 로그아웃_API_인증_실패() {
        given()
            .when()
            .post("/api/auth/logout")
            .then()
            .statusCode(401)
            .body("message", equalTo(ErrorCode.AUTH_UNAUTHORIZED.getMessage()));
    }

    @Test
    @DisplayName("지점 조회 API는 토큰이 없으면 401을 반환한다.")
    void 지점_조회_API_인증_실패() {
        given().when().get("/api/stores").then().statusCode(401)
            .body("message", equalTo(ErrorCode.AUTH_UNAUTHORIZED.getMessage()));
    }

    @Test
    @DisplayName("로그아웃 API는 유효한 토큰이면 204를 반환한다.")
    void 로그아웃_API_성공() {
        given().header("Authorization", "Bearer " + token(Role.USER))
            .when()
            .post("/api/auth/logout")
            .then()
            .statusCode(204);
    }

    @Test
    @DisplayName("공개 API는 토큰 없이 통과한다.")
    void 공개_API_인증_제외() {
        given().when().get("/api/themes").then().statusCode(200);
    }

    private String token(Role role) {
        Member member = Member.create("테스트", role.name().toLowerCase(), "password", role)
            .withId(1L);
        String token = jwtProvider.generateToken(member);
        tokenRepository.save(
            Token.create(member.getId(), token, jwtProvider.extractExpiration(token)));

        return token;
    }

    private static String secretKey() {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);

        return Base64.getEncoder().encodeToString(key);
    }
}
