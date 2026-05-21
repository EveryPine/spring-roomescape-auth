package roomescape.domain.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.reservation.entity.Reservation;

public interface ReservationRepository {

    List<Reservation> findAllReservations();

    List<Reservation> findReservationsByMemberId(Long memberId);

    List<Reservation> findReservationsByStoreIds(List<Long> storeIds);

    Optional<Reservation> findReservationById(Long id);

    Optional<Reservation> findReservationByDateTimeThemeIdAndStoreId(LocalDate date, Long timeId,
        Long themeId, Long storeId);

    List<Long> findTimeIdsByDateThemeIdAndStoreId(LocalDate localDate, Long themeId, Long storeId);

    Reservation save(Reservation reservation);

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    void updateReservationById(Long id, LocalDate date, Long timeId);

    int deleteReservationById(Long id);
}
