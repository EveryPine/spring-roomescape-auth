package roomescape.domain.reservation.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.TestAuthorizationProvider;
import roomescape.global.auth.JwtProvider;
import roomescape.global.auth.entity.Role;
import roomescape.global.auth.repository.TokenRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("관리자 예약의")
class AdminReservationApiTest {

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private TokenRepository tokenRepository;

    private TestAuthorizationProvider testAuthorizationProvider;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        testAuthorizationProvider = new TestAuthorizationProvider(jwtProvider, tokenRepository);
    }

    @Nested
    @DisplayName("예약 조회 api는")
    class GetReservationsTest {

        @Test
        @DisplayName("전체 예약을 조회한다.")
        void 성공() {
            given()
                .header("Authorization", testAuthorizationProvider.bearerToken(Role.ADMIN))
                .when()
                .get("/api/admin/reservations")
                .then()
                .statusCode(200);
        }
    }

    @Nested
    @DisplayName("예약 생성 api는")
    class SaveReservationTest {

        @Test
        @DisplayName("예약을 생성한다.")
        void 성공() {
            given()
                .header("Authorization", testAuthorizationProvider.bearerToken(Role.ADMIN))
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "memberId", 1L,
                    "date", "2026-12-31",
                    "timeId", 1L,
                    "themeId", 1L,
                    "storeId", 1L
                ))
                .when()
                .post("/api/admin/reservations")
                .then()
                .statusCode(201)
                .body("memberId", equalTo(1));
        }

        @Test
        @DisplayName("예약자 id가 누락되면 400을 반환한다.")
        void 실패1() {
            given()
                .header("Authorization", testAuthorizationProvider.bearerToken(Role.ADMIN))
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "date", "2026-12-31",
                    "timeId", 1L,
                    "themeId", 1L,
                    "storeId", 1L
                ))
                .when()
                .post("/api/admin/reservations")
                .then()
                .statusCode(400)
                .body("errors.field", hasItem("memberId"))
                .body("errors.find { it.field == 'memberId' }.value", nullValue())
                .body("errors.find { it.field == 'memberId' }.message", equalTo("예약자 id를 입력해주세요."));
        }

        @Test
        @DisplayName("필수 필드가 누락되면 400을 반환한다.")
        void 실패2() {
            given()
                .header("Authorization", testAuthorizationProvider.bearerToken(Role.ADMIN))
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "memberId", 1L,
                    "date", "2026-12-31",
                    "timeId", 1L
                ))
                .when()
                .post("/api/admin/reservations")
                .then()
                .statusCode(400)
                .body("errors.field", hasItem("themeId"))
                .body("errors.find { it.field == 'themeId' }.value", nullValue())
                .body("errors.find { it.field == 'themeId' }.message", equalTo("테마를 선택해주세요."));
        }

        @Test
        @DisplayName("날짜 형식이 잘못되면 400을 반환한다.")
        void 실패3() {
            String wrongDate = "2026/12/31";

            given()
                .header("Authorization", testAuthorizationProvider.bearerToken(Role.ADMIN))
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "memberId", 1L,
                    "date", wrongDate,
                    "timeId", 1L,
                    "themeId", 1L,
                    "storeId", 1L
                ))
                .when()
                .post("/api/admin/reservations")
                .then()
                .statusCode(400)
                .body("errors.field", hasItem("date"))
                .body("errors.find { it.field == 'date' }.value", equalTo(wrongDate))
                .body("errors.find { it.field == 'date' }.message", equalTo("형식이 올바르지 않습니다."));
        }
    }

    @Nested
    @DisplayName("예약 삭제 api는")
    class DeleteReservationTest {

        @Test
        @DisplayName("예약을 삭제한다.")
        void 성공() {
            Long id = createReservation();

            given()
                .header("Authorization", testAuthorizationProvider.bearerToken(Role.ADMIN))
                .when()
                .delete("/api/admin/reservations/{id}", id)
                .then()
                .statusCode(204);
        }
    }

    private Long createReservation() {
        return given()
            .header("Authorization", testAuthorizationProvider.bearerToken(Role.ADMIN))
            .contentType(ContentType.JSON)
            .body(Map.of(
                "memberId", 1L,
                "date", "2026-12-31",
                "timeId", 1L,
                "themeId", 1L,
                    "storeId", 1L
                ))
            .when()
            .post("/api/admin/reservations")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");
    }
}
