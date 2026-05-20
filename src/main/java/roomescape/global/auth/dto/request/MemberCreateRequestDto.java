package roomescape.global.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MemberCreateRequestDto(@NotBlank String name,
                                     @NotBlank String loginId,
                                     @NotBlank String password) {

}
