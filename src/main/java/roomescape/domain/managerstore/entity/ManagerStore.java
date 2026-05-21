package roomescape.domain.managerstore.entity;

public class ManagerStore {

    private final Long id;
    private final Long managerId;
    private final Long storeId;

    private ManagerStore(Long id, Long managerId, Long storeId) {
        this.id = id;
        this.managerId = managerId;
        this.storeId = storeId;
    }

    private ManagerStore(Long managerId, Long storeId) {
        this.id = null;
        this.managerId = managerId;
        this.storeId = storeId;
    }

    public static ManagerStore create(Long managerId, Long storeId) {
        return new ManagerStore(managerId, storeId);
    }

    public ManagerStore withId(Long id) {
        return new ManagerStore(id, this.managerId, this.storeId);
    }

    public Long getId() {
        return id;
    }

    public Long getManagerId() {
        return managerId;
    }

    public Long getStoreId() {
        return storeId;
    }
}
