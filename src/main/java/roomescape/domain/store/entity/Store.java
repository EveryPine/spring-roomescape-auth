package roomescape.domain.store.entity;

import java.util.Objects;

public class Store {

    private final Long id;
    private final String name;

    private Store(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    private Store(String name) {
        this.id = null;
        this.name = name;
    }

    public static Store create(String name) {
        return new Store(name);
    }

    public Store withId(Long id) {
        return new Store(id, this.name);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Store store = (Store) o;
        return Objects.equals(id, store.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
