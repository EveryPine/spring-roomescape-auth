package roomescape.global.auth.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.global.auth.entity.Member;
import roomescape.global.auth.entity.Role;

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
    public Optional<Member> findById(Long id) {
        String sql = "SELECT id, name, login_id, password, role FROM MEMBER WHERE id = :id";
        SqlParameterSource parameters = new MapSqlParameterSource("id", id);

        try {
            Member member = jdbcTemplate.queryForObject(sql, parameters, this::mapMember);
            return Optional.ofNullable(member);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Member> findByLoginId(String loginId) {
        String sql = "SELECT id, name, login_id, password, role FROM member WHERE login_id = :loginId";
        SqlParameterSource parameters = new MapSqlParameterSource("loginId", loginId);

        try {
            Member member = jdbcTemplate.queryForObject(sql, parameters, this::mapMember);
            return Optional.ofNullable(member);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private Member mapMember(ResultSet resultSet, int rowNum) throws SQLException {
        return Member.create(resultSet.getString("name"), resultSet.getString("login_id"),
                resultSet.getString("password"), Role.valueOf(resultSet.getString("role")))
            .withId(resultSet.getLong("id"));
    }
}
