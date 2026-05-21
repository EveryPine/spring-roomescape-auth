package roomescape.domain.store.repository;

import java.util.Optional;
import roomescape.domain.store.entity.Store;

public interface StoreRepository {

    Optional<Store> findById(Long id);

    Store save(Store store);
}
