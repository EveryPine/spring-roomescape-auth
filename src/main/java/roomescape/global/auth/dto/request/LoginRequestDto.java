package roomescape.global.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(@JsonProperty("memberId") @NotBlank String loginId,
                              @NotBlank String password) {

}
