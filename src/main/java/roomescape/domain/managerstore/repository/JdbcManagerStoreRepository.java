package roomescape.domain.managerstore.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.managerstore.entity.ManagerStore;

@Repository
public class JdbcManagerStoreRepository implements ManagerStoreRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcManagerStoreRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("manager_store")
            .usingGeneratedKeyColumns("id")
            .usingColumns("manager_id", "store_id");
    }

    @Override
    public ManagerStore save(ManagerStore managerStore) {
        SqlParameterSource parameters = new MapSqlParameterSource(Map.of(
            "manager_id", managerStore.getManagerId(),
            "store_id", managerStore.getStoreId()
        ));
        Long generatedKey = simpleJdbcInsert.executeAndReturnKey(parameters)
            .longValue();

        return managerStore.withId(generatedKey);
    }

    @Override
    public List<ManagerStore> findByManagerId(Long managerId) {
        String sql = "SELECT id, manager_id, store_id FROM manager_store WHERE manager_id = :manager_id";
        SqlParameterSource parameters = new MapSqlParameterSource("manager_id", managerId);

        return jdbcTemplate.query(sql, parameters, this::mapManagerStore);
    }

    @Override
    public boolean existsByManagerIdAndStoreId(Long managerId, Long storeId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM manager_store WHERE manager_id = :manager_id AND store_id = :store_id)";
        SqlParameterSource parameters = new MapSqlParameterSource(Map.of(
            "manager_id", managerId,
            "store_id", storeId
        ));

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, parameters, Boolean.class));
    }

    private ManagerStore mapManagerStore(ResultSet resultSet, int rowNum) throws SQLException {
        return ManagerStore.create(
                resultSet.getLong("manager_id"),
                resultSet.getLong("store_id"))
            .withId(resultSet.getLong("id"));
    }
}
