package roomescape.global.auth.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.dto.request.MemberCreateRequestDto;
import roomescape.global.auth.dto.response.MemberCreateResponseDto;
import roomescape.global.auth.entity.Member;
import roomescape.global.auth.entity.Role;
import roomescape.global.auth.service.AuthService;

@RestController
@RequestMapping("/api/members")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping()
    public ResponseEntity<MemberCreateResponseDto> createMember(
        @Valid @RequestBody MemberCreateRequestDto request) {
        Member member = authService.saveMember(request, Role.USER);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(
                new MemberCreateResponseDto(member.getId(), member.getName(), member.getLoginId()));
    }
}
