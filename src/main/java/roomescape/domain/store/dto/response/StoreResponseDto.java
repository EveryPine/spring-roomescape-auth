package roomescape.domain.store.dto.response;

import roomescape.domain.store.entity.Store;

public record StoreResponseDto(
    Long id,
    String name
) {

    public static StoreResponseDto from(Store store) {
        return new StoreResponseDto(store.getId(), store.getName());
    }
}
