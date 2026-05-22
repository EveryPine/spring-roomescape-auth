package roomescape.global.auth.repository;

import roomescape.global.auth.entity.Token;

public interface TokenRepository {

    Token save(Token token);

    int deleteByMemberId(Long memberId);
}
