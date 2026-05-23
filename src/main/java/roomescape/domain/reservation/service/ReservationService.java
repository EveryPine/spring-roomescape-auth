package roomescape.domain.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.managerstore.entity.ManagerStore;
import roomescape.domain.managerstore.repository.ManagerStoreRepository;
import roomescape.domain.reservation.dto.request.ReservationCreateRequestDto;
import roomescape.domain.reservation.dto.request.ReservationUpdateRequestDto;
import roomescape.domain.reservation.dto.request.StaffReservationCreateRequestDto;
import roomescape.domain.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.domain.reservation.dto.response.ReservationResponseDto;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.store.entity.Store;
import roomescape.domain.store.repository.StoreRepository;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.repository.TimeRepository;
import roomescape.global.error.ErrorCode;
import roomescape.global.error.ErrorDetail;
import roomescape.global.error.exception.BusinessException;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ManagerStoreRepository managerStoreRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final StoreRepository storeRepository;

    public ReservationService(ReservationRepository reservationRepository,
        ManagerStoreRepository managerStoreRepository, TimeRepository timeRepository,
        ThemeRepository themeRepository, StoreRepository storeRepository) {
        this.reservationRepository = reservationRepository;
        this.managerStoreRepository = managerStoreRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.storeRepository = storeRepository;
    }

    public List<ReservationResponseDto> getReservations() {
        List<Reservation> reservations = reservationRepository.findAllReservations();
        return convertReservationsToDto(reservations);
    }

    public List<ReservationResponseDto> getReservationsByMemberId(Long memberId) {
        List<Reservation> reservations = reservationRepository.findAllByMemberId(memberId);
        return convertReservationsToDto(reservations);
    }

    public List<ReservationResponseDto> getReservationsByManagerId(Long managerId) {
        List<Long> storeIds = managerStoreRepository.findByManagerId(managerId).stream()
            .map(ManagerStore::getStoreId).toList();
        List<Reservation> reservations = reservationRepository.findAllByStoreIds(storeIds);

        return convertReservationsToDto(reservations);
    }

    private List<ReservationResponseDto> convertReservationsToDto(List<Reservation> reservations) {
        return reservations.stream().map(ReservationResponseDto::from).toList();
    }

    @Transactional
    public ReservationCreateResponseDto saveReservation(Long memberId,
        ReservationCreateRequestDto request, LocalDateTime now) {
        Reservation reservation = createReservation(memberId, request.timeId(), request.themeId(),
            request.storeId(), request.date(), now);
        validateDuplicates(request.date(), request.timeId(), request.themeId(), request.storeId());

        return ReservationCreateResponseDto.from(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationCreateResponseDto saveManagerReservation(Long managerId,
        StaffReservationCreateRequestDto request, LocalDateTime now) {
        Reservation reservation = createReservation(request.memberId(), request.timeId(),
            request.themeId(), request.storeId(), request.date(), now);
        validateManagerStore(managerId, request.storeId());
        validateDuplicates(request.date(), request.timeId(), request.themeId(), request.storeId());

        return ReservationCreateResponseDto.from(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationCreateResponseDto saveAdminReservation(
        StaffReservationCreateRequestDto request, LocalDateTime now) {
        Long memberId = request.memberId();
        Reservation reservation = createReservation(memberId, request.timeId(), request.themeId(),
            request.storeId(), request.date(), now);
        validateDuplicates(request.date(), request.timeId(), request.themeId(), request.storeId());
        return ReservationCreateResponseDto.from(reservationRepository.save(reservation));

    }

    private void validateDuplicates(LocalDate date, Long timeId, Long themeId, Long storeId) {
        reservationRepository.findByDateTimeThemeIdAndStoreId(date, timeId, themeId, storeId)
            .ifPresent(duplicateReservation -> {
                throw new BusinessException(ErrorCode.RESERVATION_DUPLICATE);
            });
    }

    private void validateManagerStore(Long managerId, Long storeId) {
        if (!managerStoreRepository.existsByManagerIdAndStoreId(managerId, storeId)) {
            throw new BusinessException(ErrorCode.RESERVATION_FORBIDDEN);
        }
    }

    private Reservation createReservation(Long memberId, Long timeId, Long themeId, Long storeId,
        LocalDate date, LocalDateTime now) {
        Time time = getTimeById(timeId);
        Theme theme = getThemeById(themeId);
        Store store = getStoreById(storeId);

        return Reservation.create(memberId, date, time, theme, store, now);
    }

    private Store getStoreById(Long storeId) {
        return storeRepository.findById(storeId).orElseThrow(
            () -> new BusinessException(ErrorCode.COMMON_INVALID_REQUEST_BODY,
                ErrorDetail.of("storeId", storeId, "요청한 지점 id가 존재하지 않습니다.")));
    }

    private Theme getThemeById(Long themeId) {
        return themeRepository.findThemeById(themeId).orElseThrow(
            () -> new BusinessException(ErrorCode.COMMON_INVALID_REQUEST_BODY,
                ErrorDetail.of("themeId", themeId, "요청한 테마 id가 존재하지 않습니다.")));
    }

    private Time getTimeById(Long timeId) {
        return timeRepository.findTimeById(timeId).orElseThrow(
            () -> new BusinessException(ErrorCode.COMMON_INVALID_REQUEST_BODY,
                ErrorDetail.of("timeId", timeId, "요청한 시간 id가 존재하지 않습니다.")));
    }

    @Transactional
    public void updateManagerReservation(Long managerId, Long id,
        ReservationUpdateRequestDto request, LocalDateTime now) {
        Reservation reservation = getReservationById(id);
        validateManagerStore(managerId, reservation.getStore().getId());
        updateReservation(id, request, now, reservation);
    }

    @Transactional
    public void updateUserReservation(Long memberId, Long id, ReservationUpdateRequestDto request,
        LocalDateTime now) {
        Reservation reservation = getReservationById(id);
        validateOwner(memberId, reservation);
        updateReservation(id, request, now, reservation);
    }

    private void updateReservation(Long id, ReservationUpdateRequestDto request, LocalDateTime now,
        Reservation reservation) {
        Time time = getTimeById(request.timeId());
        validateDuplicatesExceptMe(id, request.date(), request.timeId(),
            reservation.getTheme().getId(), reservation.getStore().getId());
        validateDateAccessable(reservation, now);
        validateDateTimeChangeable(request.date(), time, now);

        reservationRepository.updateById(id, request.date(), request.timeId());
    }

    private Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    private void validateDuplicatesExceptMe(Long id, LocalDate date, Long timeId, Long themeId,
        Long storeId) {
        reservationRepository.findByDateTimeThemeIdAndStoreId(date, timeId, themeId, storeId)
            .filter(foundReservation -> !Objects.equals(foundReservation.getId(), id))
            .ifPresent(foundReservation -> {
                throw new BusinessException(ErrorCode.RESERVATION_DUPLICATE);
            });
    }

    private void validateDateTimeChangeable(LocalDate date, Time time, LocalDateTime now) {
        LocalDate nowDate = now.toLocalDate();
        LocalTime nowTime = now.toLocalTime();

        if (date.isBefore(nowDate) || (date.isEqual(nowDate) && time.isPast(nowTime))) {
            throw new BusinessException(ErrorCode.RESERVATION_TIME_ALREADY_PASSED);
        }
    }

    @Transactional
    public void deleteReservationById(Long id) {
        if (reservationRepository.deleteById(id) == 0) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_FOUND);
        }
    }

    @Transactional
    public void deleteMemberReservationById(Long memberId, Long id, LocalDateTime now) {
        Reservation reservation = getReservationById(id);
        validateOwner(memberId, reservation);
        validateDateAccessable(reservation, now);
        reservationRepository.deleteById(id);
    }

    @Transactional
    public void deleteManagerReservationById(Long managerId, Long id, LocalDateTime now) {
        Reservation reservation = getReservationById(id);
        validateManagerStore(managerId, reservation.getStore().getId());
        validateDateAccessable(reservation, now);
        if (reservationRepository.deleteById(id) == 0) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_FOUND);
        }
    }

    private void validateOwner(Long memberId, Reservation reservation) {
        if (!reservation.isOwner(memberId)) {
            throw new BusinessException(ErrorCode.RESERVATION_FORBIDDEN);
        }
    }

    private void validateDateAccessable(Reservation reservation, LocalDateTime now) {
        if (reservation.isPast(now)) {
            throw new BusinessException(ErrorCode.RESERVATION_ALREADY_PASSED);
        }
    }
}
