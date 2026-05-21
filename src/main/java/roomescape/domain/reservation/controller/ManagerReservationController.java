package roomescape.domain.reservation.controller;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.reservation.dto.request.ReservationUpdateRequestDto;
import roomescape.domain.reservation.dto.request.StaffReservationCreateRequestDto;
import roomescape.domain.reservation.dto.response.ReservationCreateResponseDto;
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

    @PostMapping
    public ResponseEntity<ReservationCreateResponseDto> saveReservation(@LoginMember Member member,
        @Valid @RequestBody StaffReservationCreateRequestDto request) {
        Long managerId = member.getId();
        LocalDateTime now = LocalDateTime.now();

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(reservationService.saveManagerReservation(managerId, request, now));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateReservation(@PathVariable Long id, @LoginMember Member member,
        @Valid @RequestBody ReservationUpdateRequestDto request) {
        LocalDateTime now = LocalDateTime.now();
        reservationService.updateManagerReservation(member.getId(), id, request, now);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id,
        @LoginMember Member member) {
        LocalDateTime now = LocalDateTime.now();
        reservationService.deleteManagerReservationById(member.getId(), id, now);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
