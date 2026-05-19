package roomescape.global.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record MemberCreateRequestDto(@NotBlank String name,
                                     @JsonProperty("memberId") @NotBlank String loginId,
                                     @NotBlank String password) {

}
