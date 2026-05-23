package roomescape.domain.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.reservation.entity.Reservation;

public interface ReservationRepository {

    List<Reservation> findAllReservations();

    List<Reservation> findAllByMemberId(Long memberId);

    List<Reservation> findAllByStoreIds(List<Long> storeIds);

    Optional<Reservation> findById(Long id);

    Optional<Reservation> findByDateTimeThemeIdAndStoreId(LocalDate date, Long timeId,
        Long themeId, Long storeId);

    List<Long> findTimeIdsByDateThemeIdAndStoreId(LocalDate localDate, Long themeId, Long storeId);

    Reservation save(Reservation reservation);

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    void updateById(Long id, LocalDate date, Long timeId);

    int deleteById(Long id);
}
