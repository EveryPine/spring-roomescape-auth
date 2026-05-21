package roomescape.domain.store.api;

import static io.restassured.RestAssured.given;
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
import org.springframework.test.annotation.DirtiesContext;
import roomescape.TestAuthorizationProvider;
import roomescape.global.auth.JwtProvider;
import roomescape.global.auth.entity.Role;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("사용자 매장의")
public class StoreApiTest {

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
    @DisplayName("매장 조회 api는")
    class GetAllStoresTest {

        @Test
        @DisplayName("매장을 조회한다.")
        void 성공() {
            createStore("지점");

            given()
                .header("Authorization", testAuthorizationProvider.bearerToken(Role.USER))
                .when()
                .get("/api/stores")
                .then()
                .statusCode(200)
                .body("name", hasItem("지점"));
        }

        @Test
        @DisplayName("인증 헤더가 없으면 401을 반환한다.")
        void 실패1() {
            given()
                .when()
                .get("/api/stores")
                .then()
                .statusCode(401);
        }
    }

    private Long createStore(String name) {
        return given()
            .header("Authorization", testAuthorizationProvider.bearerToken(Role.ADMIN))
            .contentType(ContentType.JSON)
            .body(Map.of("name", name))
            .when()
            .post("/api/admin/stores")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");
    }

}
