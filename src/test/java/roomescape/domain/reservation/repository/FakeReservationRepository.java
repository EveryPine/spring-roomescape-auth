package roomescape.domain.reservation.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.time.entity.Time;

public class FakeReservationRepository implements ReservationRepository {

    private final AtomicLong id = new AtomicLong(0);

    private final List<Reservation> reservations = new ArrayList<>();
    private final Map<Long, List<Long>> managerStoreIds = new HashMap<>();

    public void assignStoreToManager(Long managerId, Long storeId) {
        managerStoreIds.computeIfAbsent(managerId, ignored -> new ArrayList<>())
            .add(storeId);
    }

    @Override
    public List<Reservation> findAllReservations() {
        return new ArrayList<>(reservations);
    }

    @Override
    public List<Reservation> findAllByMemberId(Long memberId) {
        return reservations.stream()
            .filter(reservation -> reservation.getMemberId().equals(memberId))
            .toList();
    }

    @Override
    public List<Reservation> findAllByStoreIds(List<Long> storeIds) {
        return reservations.stream()
            .filter(reservation -> storeIds.contains(reservation.getStore().getId()))
            .toList();
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservations.stream()
            .filter(reservation -> reservation.getId().equals(id))
            .findFirst();
    }

    @Override
    public Optional<Reservation> findByDateTimeThemeIdAndStoreId(LocalDate date,
        Long timeId, Long themeId, Long storeId) {
        return reservations.stream()
            .filter(reservation -> Objects.equals(reservation.getDate(), date))
            .filter(reservation -> Objects.equals(reservation.getTime().getId(), timeId))
            .filter(reservation -> Objects.equals(reservation.getTheme().getId(), themeId))
            .filter(reservation -> Objects.equals(reservation.getStore().getId(), storeId))
            .findFirst();
    }

    @Override
    public List<Long> findTimeIdsByDateThemeIdAndStoreId(LocalDate localDate, Long themeId,
        Long storeId) {
        return reservations.stream()
            .filter(reservation -> reservation.getDate().equals(localDate))
            .filter(reservation -> reservation.getTheme().getId().equals(themeId))
            .filter(reservation -> reservation.getStore().getId().equals(storeId))
            .map(Reservation::getTime)
            .map(Time::getId)
            .toList();
    }

    @Override
    public Reservation save(Reservation reservation) {
        Reservation savedReservation = Reservation.create(
            reservation.getMemberId(),
            reservation.getDate(),
            reservation.getTime(),
            reservation.getTheme(),
            reservation.getStore(),
            LocalDateTime.MIN
        ).withId(id.addAndGet(1));
        reservations.add(savedReservation);
        return savedReservation;
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        return reservations.stream()
            .anyMatch(reservation -> reservation.getTime().getId().equals(timeId));
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        return reservations.stream()
            .anyMatch(reservation -> reservation.getTheme().getId().equals(themeId));
    }

    @Override
    public void updateById(Long id, LocalDate date, Long timeId) {
        for (int i = 0; i < reservations.size(); i++) {
            Reservation reservation = reservations.get(i);

            if (!reservation.getId().equals(id)) {
                continue;
            }

            Time time = reservation.getTime();
            Reservation updatedReservation = Reservation.create(
                reservation.getMemberId(),
                date,
                Time.create(time.getStartAt()).withId(timeId),
                reservation.getTheme(),
                reservation.getStore(),
                LocalDateTime.MIN
            ).withId(reservation.getId());
            reservations.set(i, updatedReservation);
            return;
        }
    }

    @Override
    public int deleteById(Long id) {
        int beforeSize = reservations.size();
        reservations.removeIf(reservation -> Objects.equals(reservation.getId(), id));

        return beforeSize - reservations.size();
    }
}
