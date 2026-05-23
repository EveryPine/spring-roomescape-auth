package roomescape.domain.reservation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.reservation.dto.request.ReservationCreateRequestDto;
import roomescape.domain.reservation.dto.request.ReservationUpdateRequestDto;
import roomescape.domain.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.domain.reservation.dto.response.ReservationResponseDto;
import roomescape.domain.reservation.service.ReservationService;
import roomescape.global.auth.annotation.LoginMember;
import roomescape.global.auth.entity.Member;

@RestController
@RequestMapping("/api/reservations")
@Validated
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDto>> getReservations(
        @LoginMember Member member) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(reservationService.getReservationsByMemberId(member.getId()));
    }

    @PostMapping
    public ResponseEntity<ReservationCreateResponseDto> saveReservation(
        @LoginMember Member member,
        @Valid @RequestBody ReservationCreateRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(reservationService.saveReservation(member.getId(), requestDto,
                LocalDateTime.now()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateReservation(
        @LoginMember Member member,
        @PathVariable Long id,
        @Valid @RequestBody ReservationUpdateRequestDto requestDto) {
        reservationService.updateUserReservation(member.getId(), id, requestDto,
            LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
        @LoginMember Member member,
        @PathVariable @Min(value = 1, message = "예약 id는 1 이상이어야 합니다.") Long id) {
        reservationService.deleteMemberReservationById(member.getId(), id, LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
