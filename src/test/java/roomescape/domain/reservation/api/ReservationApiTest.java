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
import roomescape.global.error.TypeMismatchMessage;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("사용자 예약의")
class ReservationApiTest {

    @Autowired
    private JwtProvider jwtProvider;

    private TestAuthorizationProvider testAuthorizationProvider;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        testAuthorizationProvider = new TestAuthorizationProvider(jwtProvider);
    }

    @Nested
    @DisplayName("예약 조회 api는")
    class GetReservationsTest {

        @Test
        @DisplayName("예약을 조회한다.")
        void 성공() {
            createReservation("2026-12-31", 1L, 1L);

            given()
                .header("Authorization", testAuthorizationProvider.bearerToken(Role.USER))
                .when()
                .get("/api/reservations")
                .then()
                .statusCode(200)
                .body("date", hasItem("2026-12-31"));
        }

        @Test
        @DisplayName("인증 헤더가 없으면 401을 반환한다.")
        void 실패1() {
            given()
                .when()
                .get("/api/reservations")
                .then()
                .statusCode(401);
        }
    }

    @Nested
    @DisplayName("예약 생성 api는")
    class SaveReservationTest {

        @Test
        @DisplayName("정상 요청이면 201을 반환한다.")
        void 성공() {
            given()
                .header("Authorization", testAuthorizationProvider.bearerToken(Role.USER))
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "date", "2026-12-31",
                    "timeId", 1L,
                    "themeId", 1L
                ))
                .when()
                .post("/api/reservations")
                .then()
                .statusCode(201)
                .body("memberId", equalTo(1));
        }

        @Test
        @DisplayName("예약 날짜가 누락되면 400을 반환한다.")
        void 실패1() {
            given()
                .header("Authorization", testAuthorizationProvider.bearerToken(Role.USER))
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "timeId", 1L,
                    "themeId", 1L
                ))
                .when()
                .post("/api/reservations")
                .then()
                .statusCode(400)
                .body("errors.field", hasItem("date"))
                .body("errors.find { it.field == 'date' }.value", nullValue())
                .body("errors.find { it.field == 'date' }.message", equalTo("예약 날짜를 입력해주세요."));
        }

        @Test
        @DisplayName("필수 필드가 누락되면 400을 반환한다.")
        void 실패2() {
            given()
                .header("Authorization", testAuthorizationProvider.bearerToken(Role.USER))
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "date", "2026-12-31",
                    "timeId", 1L
                ))
                .when()
                .post("/api/reservations")
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
                .header("Authorization", testAuthorizationProvider.bearerToken(Role.USER))
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "date", wrongDate,
                    "timeId", 1L,
                    "themeId", 1L
                ))
                .when()
                .post("/api/reservations")
                .then()
                .statusCode(400)
                .body("errors.field", hasItem("date"))
                .body("errors.find { it.field == 'date' }.value", equalTo(wrongDate))
                .body("errors.find { it.field == 'date' }.message", equalTo("형식이 올바르지 않습니다."));
        }
    }

    @Nested
    @DisplayName("예약 수정 api는")
    class UpdateReservationTest {

        @Test
        @DisplayName("정상 요청이면 204를 반환한다.")
        void 성공() {
            Long id = createReservation("2026-12-30", 1L, 1L);

            given()
                .header("Authorization", testAuthorizationProvider.bearerToken(Role.USER))
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "date", "2026-12-31",
                    "timeId", 2L
                ))
                .when()
                .patch("/api/reservations/{id}", id)
                .then()
                .statusCode(204);
        }

        @Test
        @DisplayName("인증 헤더가 없으면 401을 반환한다.")
        void 실패1() {
            Long id = createReservation("2026-12-30", 1L, 1L);

            given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "date", "2026-12-31",
                    "timeId", 2L
                ))
                .when()
                .patch("/api/reservations/{id}", id)
                .then()
                .statusCode(401);
        }

        @Test
        @DisplayName("요청 경로 변수가 잘못되면 400을 반환한다.")
        void 실패2() {
            Object wrongId = "a";

            given()
                .header("Authorization", testAuthorizationProvider.bearerToken(Role.USER))
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "timeId", 2L
                ))
                .when()
                .patch("/api/reservations/{id}", wrongId)
                .then()
                .statusCode(400)
                .body("errors.field", hasItem("id"))
                .body("errors.find { it.field == 'id' }.value", equalTo(wrongId))
                .body("errors.find { it.field == 'id' }.message",
                    equalTo(TypeMismatchMessage.from(Long.class)));
        }

        @Test
        @DisplayName("요청 본문 필드 중 하나가 누락되면 400을 반환한다.")
        void 실패3() {
            Long id = createReservation("2026-12-30", 1L, 1L);

            given()
                .header("Authorization", testAuthorizationProvider.bearerToken(Role.USER))
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "timeId", 2L
                ))
                .when()
                .patch("/api/reservations/{id}", id)
                .then()
                .statusCode(400)
                .body("errors.field", hasItem("date"))
                .body("errors.find { it.field == 'date' }.value", nullValue())
                .body("errors.find { it.field == 'date' }.message", equalTo("변경할 예약 날짜를 입력해주세요."));
        }

        @Test
        @DisplayName("날짜 형식이 잘못되면 400을 반환한다.")
        void 실패4() {
            Long id = createReservation("2026-12-30", 1L, 1L);
            String wrongDate = "2026/12/31";

            given()
                .header("Authorization", testAuthorizationProvider.bearerToken(Role.USER))
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "date", wrongDate,
                    "timeId", 2L
                ))
                .when()
                .patch("/api/reservations/{id}", id)
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
        @DisplayName("정상 요청이면 204를 반환한다.")
        void 성공() {
            Long id = createReservation("2026-12-31", 1L, 1L);

            given()
                .header("Authorization", testAuthorizationProvider.bearerToken(Role.USER))
                .when()
                .delete("/api/reservations/{id}", id)
                .then()
                .statusCode(204);
        }

        @Test
        @DisplayName("인증 헤더가 없으면 401을 반환한다.")
        void 실패1() {
            Long id = createReservation("2026-12-31", 1L, 1L);

            given()
                .when()
                .delete("/api/reservations/{id}", id)
                .then()
                .statusCode(401);
        }
    }

    private Long createReservation(String date, Long timeId, Long themeId) {
        return given()
            .header("Authorization", testAuthorizationProvider.bearerToken(Role.USER))
            .contentType(ContentType.JSON)
            .body(Map.of(
                "date", date,
                "timeId", timeId,
                "themeId", themeId
            ))
            .when()
            .post("/api/reservations")
            .then()
            .statusCode(201)
            .body("memberId", equalTo(1))
            .extract()
            .jsonPath()
            .getLong("id");
    }
}
