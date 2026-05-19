package roomescape.global.auth.repository;

import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.global.auth.entity.Member;

@Repository
public class JdbcMemberRepository implements MemberRepository {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert simpleJdbcInsert;

    public JdbcMemberRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("member")
            .usingColumns("name", "login_id", "password", "role")
            .usingGeneratedKeyColumns("id");
    }

    @Override
    public Member save(Member member) {
        SqlParameterSource parameters = new MapSqlParameterSource(Map.of(
            "name", member.getName(),
            "login_id", member.getLoginId(),
            "password", member.getPassword(),
            "role", member.getRole().name()
        ));
        Long generatedKey = simpleJdbcInsert.executeAndReturnKey(parameters)
            .longValue();

        return member.withId(generatedKey);
    }

    @Override
    public boolean existsByLoginId(String loginId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM member WHERE login_id = :login_id)";
        SqlParameterSource parameters = new MapSqlParameterSource("login_id", loginId);

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, parameters, Boolean.class));
    }
}
