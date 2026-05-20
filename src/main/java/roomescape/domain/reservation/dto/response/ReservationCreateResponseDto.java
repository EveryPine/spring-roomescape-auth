package roomescape.domain.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.store.entity.Store;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.time.entity.Time;

public record ReservationCreateResponseDto(Long id, Long memberId,
                                           @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                           Long timeId,
                                           Long themeId,
                                           Long storeId) {

    public static ReservationCreateResponseDto from(Reservation reservation) {
        Time time = reservation.getTime();
        Theme theme = reservation.getTheme();
        Store store = reservation.getStore();

        return new ReservationCreateResponseDto(reservation.getId(), reservation.getMemberId(),
            reservation.getDate(), time.getId(), theme.getId(), store.getId());
    }
}
