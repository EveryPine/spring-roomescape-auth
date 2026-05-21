package roomescape.domain.store.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.store.entity.Store;

public class FakeStoreRepository implements StoreRepository {

    private final AtomicLong id = new AtomicLong(0);
    private final List<Store> stores = new ArrayList<>();

    public FakeStoreRepository() {
        save(Store.create("강남점"));
    }

    @Override
    public Store save(Store store) {
        Store savedStore = Store.create(store.getName()).withId(id.addAndGet(1));
        stores.add(savedStore);
        return savedStore;
    }

    @Override
    public Optional<Store> findById(Long id) {
        return stores.stream()
            .filter(store -> Objects.equals(store.getId(), id))
            .findFirst();
    }
}
