package roomescape.domain.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.store.dto.response.StoreResponseDto;
import roomescape.domain.store.entity.Store;
import roomescape.domain.theme.dto.response.ThemeResponseDto;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.time.dto.response.TimeResponseDto;
import roomescape.domain.time.entity.Time;

public record ReservationResponseDto(Long id, Long memberId,
                                     @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                     TimeResponseDto time, ThemeResponseDto theme,
                                     StoreResponseDto store) {


    public static ReservationResponseDto from(Reservation reservation) {
        Time time = reservation.getTime();
        Theme theme = reservation.getTheme();
        Store store = reservation.getStore();

        return new ReservationResponseDto(reservation.getId(), reservation.getMemberId(),
            reservation.getDate(), TimeResponseDto.from(time), ThemeResponseDto.from(theme),
            StoreResponseDto.from(store));
    }
}
