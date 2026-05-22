package roomescape.domain.reservation.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.TestAuthorizationProvider;
import roomescape.global.auth.JwtProvider;
import roomescape.global.auth.entity.Role;
import roomescape.global.auth.repository.TokenRepository;
import roomescape.global.error.ErrorCode;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("매니저 예약의")
class ManagerReservationApiTest {

    private static final Long MANAGER_ID = 201L;
    private static final Long USER_ID = 1L;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

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
        @DisplayName("담당 매장의 예약만 조회한다.")
        void 성공() {
            given()
                .header("Authorization",
                    testAuthorizationProvider.bearerTokenWithMemberId(Role.MANAGER, MANAGER_ID))
                .when()
                .get("/api/manager/reservations")
                .then()
                .statusCode(200)
                .body("size()", equalTo(100))
                .body("id", hasItem(1));
        }
    }

    @Nested
    @DisplayName("예약 생성 api는")
    class SaveReservationTest {

        @Test
        @DisplayName("담당 매장의 예약을 생성한다.")
        void 성공() {
            given()
                .header("Authorization",
                    testAuthorizationProvider.bearerTokenWithMemberId(Role.MANAGER, MANAGER_ID))
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "memberId", USER_ID,
                    "date", "2026-12-31",
                    "timeId", 1L,
                    "themeId", 1L,
                    "storeId", 1L
                ))
                .when()
                .post("/api/manager/reservations")
                .then()
                .statusCode(201)
                .body("memberId", equalTo(USER_ID.intValue()))
                .body("storeId", equalTo(1));
        }

        @Test
        @DisplayName("담당하지 않는 매장의 예약 생성을 시도하면 403을 반환한다.")
        void 실패() {
            given()
                .header("Authorization",
                    testAuthorizationProvider.bearerTokenWithMemberId(Role.MANAGER, MANAGER_ID))
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "memberId", USER_ID,
                    "date", "2026-12-31",
                    "timeId", 1L,
                    "themeId", 1L,
                    "storeId", 2L
                ))
                .when()
                .post("/api/manager/reservations")
                .then()
                .statusCode(403)
                .body("message", equalTo(ErrorCode.RESERVATION_FORBIDDEN.getMessage()));
        }
    }

    @Nested
    @DisplayName("예약 수정 api는")
    class UpdateReservationTest {

        @Test
        @DisplayName("담당 매장의 예약을 수정한다.")
        void 성공() {
            updateReservationDate(1L, "2026-12-31");

            given()
                .header("Authorization",
                    testAuthorizationProvider.bearerTokenWithMemberId(Role.MANAGER, MANAGER_ID))
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "date", "2026-12-30",
                    "timeId", 2L
                ))
                .when()
                .patch("/api/manager/reservations/{id}", 1L)
                .then()
                .statusCode(204);
        }

        @Test
        @DisplayName("담당하지 않는 매장의 예약 수정을 시도하면 403을 반환한다.")
        void 실패() {
            Long id = createAdminReservation(2L);

            given()
                .header("Authorization",
                    testAuthorizationProvider.bearerTokenWithMemberId(Role.MANAGER, MANAGER_ID))
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "date", "2026-12-30",
                    "timeId", 2L
                ))
                .when()
                .patch("/api/manager/reservations/{id}", id)
                .then()
                .statusCode(403)
                .body("message", equalTo(ErrorCode.RESERVATION_FORBIDDEN.getMessage()));
        }
    }

    @Nested
    @DisplayName("예약 삭제 api는")
    class DeleteReservationTest {

        @Test
        @DisplayName("담당 매장의 예약을 삭제한다.")
        void 성공() {
            updateReservationDate(1L, "2026-12-31");

            given()
                .header("Authorization",
                    testAuthorizationProvider.bearerTokenWithMemberId(Role.MANAGER, MANAGER_ID))
                .when()
                .delete("/api/manager/reservations/{id}", 1L)
                .then()
                .statusCode(204);
        }

        @Test
        @DisplayName("담당하지 않는 매장의 예약 삭제를 시도하면 403을 반환한다.")
        void 실패() {
            Long id = createAdminReservation(2L);

            given()
                .header("Authorization",
                    testAuthorizationProvider.bearerTokenWithMemberId(Role.MANAGER, MANAGER_ID))
                .when()
                .delete("/api/manager/reservations/{id}", id)
                .then()
                .statusCode(403)
                .body("message", equalTo(ErrorCode.RESERVATION_FORBIDDEN.getMessage()));
        }
    }

    private Long createAdminReservation(Long storeId) {
        return given()
            .header("Authorization",
                testAuthorizationProvider.bearerTokenWithMemberId(Role.ADMIN, 200L))
            .contentType(ContentType.JSON)
            .body(Map.of(
                "memberId", USER_ID,
                "date", "2026-12-31",
                "timeId", 1L,
                "themeId", 1L,
                "storeId", storeId
            ))
            .when()
            .post("/api/admin/reservations")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");
    }

    private void updateReservationDate(Long id, String date) {
        String sql = "UPDATE reservation SET date = :date WHERE id = :id";
        SqlParameterSource parameters = new MapSqlParameterSource(Map.of(
            "date", date,
            "id", id
        ));
        jdbcTemplate.update(sql, parameters);
    }
}
