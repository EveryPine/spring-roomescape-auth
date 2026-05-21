package roomescape.domain.store.dto.request;

import jakarta.validation.constraints.NotBlank;

public record StoreCreateRequestDto(@NotBlank(message = "지점명을 입력해주세요.") String name) {

}
