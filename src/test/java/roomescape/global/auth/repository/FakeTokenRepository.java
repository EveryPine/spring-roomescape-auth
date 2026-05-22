package roomescape.global.auth.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.global.auth.entity.Token;

public class FakeTokenRepository implements TokenRepository {

    private final AtomicLong id = new AtomicLong(0);
    private final List<Token> tokens = new ArrayList<>();

    @Override
    public boolean existsByToken(String tokenString) {
        return tokens.stream()
            .anyMatch(token -> token.getToken().equals(tokenString));
    }

    @Override
    public Token save(Token token) {
        Token savedToken = token.withId(id.addAndGet(1));
        tokens.add(savedToken);
        return savedToken;
    }

    @Override
    public int deleteByMemberId(Long memberId) {
        int beforeSize = tokens.size();
        tokens.removeIf(token -> Objects.equals(token.getMemberId(), memberId));

        return beforeSize - tokens.size();
    }

    public Optional<Token> findByMemberId(Long memberId) {
        return tokens.stream()
            .filter(token -> Objects.equals(token.getMemberId(), memberId))
            .findFirst();
    }

    public List<Token> findAllByMemberId(Long memberId) {
        return tokens.stream()
            .filter(token -> Objects.equals(token.getMemberId(), memberId))
            .toList();
    }
}
