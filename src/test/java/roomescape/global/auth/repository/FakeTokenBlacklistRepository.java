package roomescape.global.auth.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.global.auth.entity.TokenBlacklist;

public class FakeTokenBlacklistRepository implements TokenBlacklistRepository {

    private final AtomicLong id = new AtomicLong(0);
    private final List<TokenBlacklist> blacklists = new ArrayList<>();

    @Override
    public TokenBlacklist save(TokenBlacklist blacklist) {
        TokenBlacklist savedBlacklist = blacklist.withId(id.incrementAndGet());
        blacklists.add(savedBlacklist);
        return savedBlacklist;
    }

    @Override
    public boolean existsByToken(String token) {
        return blacklists.stream()
            .anyMatch(blacklist -> Objects.equals(blacklist.getToken(), token));
    }
}
