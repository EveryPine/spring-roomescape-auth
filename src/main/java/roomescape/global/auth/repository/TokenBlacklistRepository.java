package roomescape.global.auth.repository;

import roomescape.global.auth.entity.TokenBlacklist;

public interface TokenBlacklistRepository {

    TokenBlacklist save(TokenBlacklist blacklist);

    boolean existsByToken(String token);
}
