package roomescape.domain.store.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.store.entity.Store;

@Repository
public class JdbcStoreRepository implements StoreRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcStoreRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("store")
            .usingGeneratedKeyColumns("id")
            .usingColumns("name");
    }

    @Override
    public List<Store> findAll() {
        String sql = "SELECT id, name FROM store";

        return jdbcTemplate.query(sql, this::mapStore);
    }

    @Override
    public Optional<Store> findById(Long id) {
        String sql = "SELECT id, name FROM store WHERE id = :id";
        SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        try {
            Store store = jdbcTemplate.queryForObject(sql, parameters, this::mapStore);
            return Optional.ofNullable(store);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Store save(Store store) {
        SqlParameterSource parameters = new MapSqlParameterSource("name", store.getName());
        Long generatedKey = simpleJdbcInsert.executeAndReturnKey(parameters)
            .longValue();

        return store.withId(generatedKey);
    }

    private Store mapStore(ResultSet resultSet, int rowNum) throws SQLException {
        return Store.create(resultSet.getString("name"))
            .withId(resultSet.getLong("id"));
    }
}
