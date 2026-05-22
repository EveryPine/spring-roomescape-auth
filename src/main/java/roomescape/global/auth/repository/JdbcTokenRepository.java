package roomescape.global.auth.repository;

import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.global.auth.entity.Token;

@Repository
public class JdbcTokenRepository implements TokenRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcTokenRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("token")
            .usingGeneratedKeyColumns("id")
            .usingColumns("token", "expired_at");
    }

    @Override
    public Token save(Token token) {
        SqlParameterSource parameters = new MapSqlParameterSource(Map.of(
            "token", token.getToken(),
            "expired_at", token.getExpiredAt()
        ));
        Long generatedKey = simpleJdbcInsert.executeAndReturnKey(parameters)
            .longValue();

        return token.withId(generatedKey);
    }
}
