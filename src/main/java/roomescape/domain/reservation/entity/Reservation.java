package roomescape.domain.reservation.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.time.entity.Time;
import roomescape.global.error.ErrorCode;
import roomescape.global.error.exception.BusinessException;

public class Reservation {

    private final Long id;
    private final Long memberId;
    private final LocalDate date;
    private final Time time;
    private final Theme theme;

    private Reservation(Long id, Long memberId, LocalDate date, Time time, Theme theme,
        LocalDateTime now) {
        validateDateTime(date, time, now);
        this.id = id;
        this.memberId = memberId;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    private Reservation(Long id, Long memberId, LocalDate date, Time time, Theme theme) {
        this.id = id;
        this.memberId = memberId;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }


    public static Reservation create(Long memberId, LocalDate date, Time time,
        Theme theme, LocalDateTime now) {
        return new Reservation(null, memberId, date, time, theme, now);
    }

    public Reservation withId(Long id) {
        return new Reservation(id, this.memberId, this.date, this.time, this.theme);
    }

    private void validateDateTime(LocalDate date, Time time, LocalDateTime now) {
        LocalDate nowDate = now.toLocalDate();
        LocalTime nowTime = now.toLocalTime();

        if (date.isBefore(nowDate) || (date.isEqual(nowDate) && time.isPast(nowTime))) {
            throw new BusinessException(ErrorCode.RESERVATION_ALREADY_PASSED);
        }
    }

    public boolean isPast(LocalDateTime now) {
        LocalDate nowDate = now.toLocalDate();
        LocalTime nowTime = now.toLocalTime();

        return date.isBefore(nowDate) || (date.isEqual(nowDate) && time.isPast(nowTime));
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public boolean isOwner(Long memberId) {
        return Objects.equals(this.memberId, memberId);
    }

    public LocalDate getDate() {
        return date;
    }

    public Time getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
