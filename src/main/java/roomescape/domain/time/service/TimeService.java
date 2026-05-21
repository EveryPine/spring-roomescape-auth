package roomescape.domain.time.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.store.repository.StoreRepository;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.domain.time.dto.request.TimeCreateRequestDto;
import roomescape.domain.time.dto.response.TimeResponseDto;
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.repository.TimeRepository;
import roomescape.global.error.ErrorCode;
import roomescape.global.error.ErrorDetail;
import roomescape.global.error.exception.BusinessException;

@Service
public class TimeService {

    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final StoreRepository storeRepository;
    private final TimeRepository timeRepository;

    public TimeService(ReservationRepository reservationRepository, ThemeRepository themeRepository,
        StoreRepository storeRepository, TimeRepository timeRepository) {
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
        this.storeRepository = storeRepository;
        this.timeRepository = timeRepository;
    }

    @Transactional
    public List<TimeResponseDto> getTimes() {
        return timeRepository.findAllTimes()
            .stream()
            .map(TimeResponseDto::from)
            .toList();
    }

    @Transactional
    public List<TimeResponseDto> getAvailableTimes(LocalDate date, Long themeId, Long storeId,
        LocalDateTime now) {
        validateDate(date, now.toLocalDate());
        validateThemeId(themeId);
        validateStoreId(storeId);
        List<Long> reservedTimeIds = reservationRepository.findTimeIdsByDateThemeIdAndStoreId(date,
            themeId, storeId);

        return timeRepository.findAllTimes()
            .stream()
            .filter(time -> !reservedTimeIds.contains(time.getId()))
            .filter(time -> !date.isEqual(now.toLocalDate()) || !time.isPast(now.toLocalTime()))
            .map(TimeResponseDto::from)
            .toList();
    }

    private void validateDate(LocalDate date, LocalDate now) {
        if (date.isBefore(now)) {
            throw new BusinessException(ErrorCode.TIME_INVALID_DATE);
        }
    }

    private void validateThemeId(Long themeId) {
        if (!themeRepository.existsById(themeId)) {
            throw new BusinessException(ErrorCode.COMMON_INVALID_REQUEST,
                ErrorDetail.of("themeId", themeId, "요청한 테마 id가 존재하지 않습니다."));
        }
    }

    private void validateStoreId(Long storeId) {
        if (storeRepository.findById(storeId).isEmpty()) {
            throw new BusinessException(ErrorCode.COMMON_INVALID_REQUEST,
                ErrorDetail.of("storeId", storeId, "요청한 지점 id가 존재하지 않습니다."));
        }
    }

    @Transactional
    public TimeResponseDto saveTime(TimeCreateRequestDto requestDto) {
        Time time = Time.create(requestDto.startAt());
        if (timeRepository.existsByStartAt(time.getStartAt())) {
            throw new BusinessException(ErrorCode.TIME_DUPLICATE);
        }

        return TimeResponseDto.from(timeRepository.save(time));
    }

    @Transactional
    public void deleteTimeById(Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new BusinessException(ErrorCode.TIME_REFERENCED_BY_RESERVATION);
        }
        if (timeRepository.deleteTimeById(id) == 0) {
            throw new BusinessException(ErrorCode.TIME_NOT_FOUND);
        }
    }
}
