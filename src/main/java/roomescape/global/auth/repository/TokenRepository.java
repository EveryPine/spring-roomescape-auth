package roomescape.global.auth.repository;

import roomescape.global.auth.entity.Token;

public interface TokenRepository {

    boolean existsByToken(String token);

    Token save(Token token);

    int deleteByMemberId(Long memberId);
}
