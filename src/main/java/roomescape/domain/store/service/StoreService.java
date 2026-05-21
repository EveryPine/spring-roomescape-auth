package roomescape.domain.store.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.store.dto.request.StoreCreateRequestDto;
import roomescape.domain.store.dto.response.StoreResponseDto;
import roomescape.domain.store.entity.Store;
import roomescape.domain.store.repository.StoreRepository;

@Service
public class StoreService {

    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public List<StoreResponseDto> getStores() {
        return storeRepository.findAll()
            .stream()
            .map(StoreResponseDto::from)
            .toList();
    }

    public StoreResponseDto saveStore(StoreCreateRequestDto request) {
        Store store = Store.create(request.name());

        return StoreResponseDto.from(storeRepository.save(store));
    }
}
