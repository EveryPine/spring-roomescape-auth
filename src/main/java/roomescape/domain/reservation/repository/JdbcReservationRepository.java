package roomescape.domain.reservation.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.store.entity.Store;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.time.entity.Time;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("reservation")
            .usingColumns("member_id", "date", "time_id", "theme_id", "store_id")
            .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Reservation> findAllReservations() {
        String sql = """
            SELECT r.id, r.member_id, m.name AS member_name, r.date,
                   rt.id AS time_id, rt.start_at,
                   t.id AS theme_id, t.name AS theme_name, t.description, t.image_url,
                   s.id AS store_id, s.name AS store_name
            FROM reservation r
            JOIN member m ON r.member_id = m.id
            JOIN reservation_time rt ON r.time_id = rt.id
            JOIN theme t ON r.theme_id = t.id
            JOIN store s ON r.store_id = s.id
            """;
        return jdbcTemplate.query(sql, this::mapReservation);
    }

    @Override
    public List<Reservation> findReservationsByMemberId(Long memberId) {
        String sql = """
            SELECT r.id, r.member_id, m.name AS member_name, r.date,
                   rt.id AS time_id, rt.start_at,
                   t.id AS theme_id, t.name AS theme_name, t.description, t.image_url,
                   s.id AS store_id, s.name AS store_name
            FROM reservation r
            JOIN member m ON r.member_id = m.id
            JOIN reservation_time rt ON r.time_id = rt.id
            JOIN theme t ON r.theme_id = t.id
            JOIN store s ON r.store_id = s.id
            WHERE r.member_id = :memberId
            """;
        SqlParameterSource parameters = new MapSqlParameterSource("memberId", memberId);

        return jdbcTemplate.query(sql, parameters, this::mapReservation);
    }

    @Override
    public List<Reservation> findReservationsByStoreIds(List<Long> storeIds) {
        if (storeIds.isEmpty()) {
            return Collections.emptyList();
        }
        String sql = """
            SELECT r.id, r.member_id, r.date,
                   rt.id AS time_id, rt.start_at,
                   t.id AS theme_id, t.name AS theme_name, t.description, t.image_url,
                   s.id AS store_id, s.name AS store_name
            FROM reservation r
            JOIN reservation_time rt ON r.time_id = rt.id
            JOIN theme t ON r.theme_id = t.id
            JOIN store s ON r.store_id = s.id
            WHERE r.store_id in (:storeIds)
            """;
        SqlParameterSource parameters = new MapSqlParameterSource("storeIds", storeIds);

        return jdbcTemplate.query(sql, parameters, this::mapReservation);
    }

    @Override
    public Optional<Reservation> findReservationById(Long id) {
        String sql = """
            SELECT r.id, r.member_id, m.name AS member_name, r.date,
                   rt.id AS time_id, rt.start_at,
                   t.id AS theme_id, t.name AS theme_name, t.description, t.image_url,
                   s.id AS store_id, s.name AS store_name
            FROM reservation r
            JOIN member m ON r.member_id = m.id
            JOIN reservation_time rt ON r.time_id = rt.id
            JOIN theme t ON r.theme_id = t.id
            JOIN store s ON r.store_id = s.id
            WHERE r.id = :id
            """;
        SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        try {
            Reservation reservation = jdbcTemplate.queryForObject(sql, parameters,
                this::mapReservation);
            return Optional.ofNullable(reservation);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Reservation> findReservationByDateTimeThemeIdAndStoreId(LocalDate date,
        Long timeId, Long themeId, Long storeId) {
        String sql = """
            SELECT r.id, r.member_id, m.name AS member_name, r.date,
                   rt.id AS time_id, rt.start_at,
                   t.id AS theme_id, t.name AS theme_name, t.description, t.image_url,
                   s.id AS store_id, s.name AS store_name
            FROM reservation r
            JOIN member m ON r.member_id = m.id
            JOIN reservation_time rt ON r.time_id = rt.id
            JOIN theme t ON r.theme_id = t.id
            JOIN store s ON r.store_id = s.id
            WHERE r.date = :date AND rt.id = :timeId AND t.id = :themeId AND s.id = :storeId
            """;
        SqlParameterSource parameters = new MapSqlParameterSource(
            Map.of("date", date, "timeId", timeId, "themeId", themeId, "storeId", storeId));
        try {
            Reservation reservation = jdbcTemplate.queryForObject(sql, parameters,
                this::mapReservation);
            return Optional.ofNullable(reservation);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Long> findTimeIdsByDateThemeIdAndStoreId(LocalDate date, Long themeId,
        Long storeId) {
        String sql = """
            SELECT time_id
            FROM reservation
            WHERE date = :date AND theme_id = :themeId AND store_id = :storeId
            """;
        SqlParameterSource parameters = new MapSqlParameterSource(
            Map.of("date", date, "themeId", themeId, "storeId", storeId));

        return jdbcTemplate.query(sql, parameters,
            (resultSet, rowNum) -> resultSet.getLong("time_id"));
    }

    @Override
    public Reservation save(Reservation reservation) {
        Map<String, Object> args = Map.of("member_id", reservation.getMemberId(), "date",
            reservation.getDate(), "time_id", reservation.getTime().getId(), "theme_id",
            reservation.getTheme().getId(), "store_id", reservation.getStore().getId());
        Long generatedKey = simpleJdbcInsert.executeAndReturnKey(args).longValue();

        return reservation.withId(generatedKey);
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE time_id = :timeId)";
        SqlParameterSource parameters = new MapSqlParameterSource("timeId", timeId);

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, parameters, Boolean.class));
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE theme_id = :themeId)";
        SqlParameterSource parameters = new MapSqlParameterSource("themeId", themeId);

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, parameters, Boolean.class));
    }

    @Override
    public void updateReservationById(Long id, LocalDate date, Long timeId) {
        String sql = "UPDATE reservation SET date = :date, time_id = :timeId WHERE id = :id";
        SqlParameterSource parameters = new MapSqlParameterSource(
            Map.of("date", date, "timeId", timeId, "id", id));

        jdbcTemplate.update(sql, parameters);
    }

    @Override
    public int deleteReservationById(Long id) {
        String sql = "DELETE FROM reservation WHERE id = :id";
        SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        return jdbcTemplate.update(sql, parameters);
    }

    private Reservation mapReservation(ResultSet resultSet, int rowNum) throws SQLException {
        return Reservation.create(
            resultSet.getLong("member_id"),
            resultSet.getDate("date").toLocalDate(),
            Time.create(LocalTime.parse(resultSet.getString("start_at")))
                .withId(resultSet.getLong("time_id")),
            Theme.create(
                    resultSet.getString("theme_name"),
                    resultSet.getString("description"),
                    resultSet.getString("image_url"))
                .withId(resultSet.getLong("theme_id")),
            Store.create(resultSet.getString("name"))
                .withId(resultSet.getLong("id")),
            LocalDateTime.MIN
        ).withId(resultSet.getLong("id"));
    }
}
