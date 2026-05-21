package roomescape.domain.reservation.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.reservation.dto.response.ReservationResponseDto;
import roomescape.domain.reservation.service.ReservationService;
import roomescape.global.auth.annotation.LoginMember;
import roomescape.global.auth.entity.Member;

@RestController
@RequestMapping("/api/manager/reservations")
public class ManagerReservationController {

    private final ReservationService reservationService;

    public ManagerReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDto>> getReservations(
        @LoginMember Member member
    ) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(reservationService.getReservationsByManagerId(member.getId()));
    }
}
