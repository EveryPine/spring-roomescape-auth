package roomescape.domain.managerstore.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.managerstore.entity.ManagerStore;

public class FakeManagerStoreRepository implements ManagerStoreRepository {

    private final AtomicLong id = new AtomicLong(0);
    private final List<ManagerStore> managerStores = new ArrayList<>();

    @Override
    public ManagerStore save(ManagerStore managerStore) {
        ManagerStore savedManagerStore = ManagerStore.create(managerStore.getManagerId(),
                managerStore.getStoreId())
            .withId(id.addAndGet(1));
        managerStores.add(savedManagerStore);

        return savedManagerStore;
    }

    @Override
    public List<ManagerStore> findByManagerId(Long managerId) {
        return managerStores.stream()
            .filter(managerStore -> Objects.equals(managerStore.getManagerId(), managerId))
            .toList();
    }

    @Override
    public boolean existsByManagerIdAndStoreId(Long managerId, Long storeId) {
        return managerStores.stream()
            .anyMatch(managerStore -> Objects.equals(managerStore.getManagerId(), managerId)
                && Objects.equals(managerStore.getStoreId(), storeId));
    }
}
