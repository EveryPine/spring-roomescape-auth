package roomescape.global.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MemberCreateResponseDto(Long id, String name,
                                      @JsonProperty("memberId") String loginId) {

}
