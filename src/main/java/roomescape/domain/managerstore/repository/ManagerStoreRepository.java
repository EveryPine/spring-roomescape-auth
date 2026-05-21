package roomescape.domain.managerstore.repository;

import java.util.List;
import roomescape.domain.managerstore.entity.ManagerStore;

public interface ManagerStoreRepository {

    ManagerStore save(ManagerStore managerStore);

    List<ManagerStore> findByManagerId(Long managerId);
}
