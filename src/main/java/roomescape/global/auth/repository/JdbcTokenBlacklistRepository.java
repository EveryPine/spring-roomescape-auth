package roomescape.global.auth.repository;

import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.global.auth.entity.TokenBlacklist;

@Repository
public class JdbcTokenBlacklistRepository implements TokenBlacklistRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcTokenBlacklistRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("token_blacklist")
            .usingGeneratedKeyColumns("id")
            .usingColumns("token", "expired_at");
    }

    @Override
    public TokenBlacklist save(TokenBlacklist tokenBlacklist) {
        SqlParameterSource parameters = new MapSqlParameterSource(Map.of(
            "token", tokenBlacklist.getToken(),
            "expired_at", tokenBlacklist.getExpiredAt()
        ));
        Long generatedKey = simpleJdbcInsert.executeAndReturnKey(parameters)
            .longValue();

        return tokenBlacklist.withId(generatedKey);
    }
}
