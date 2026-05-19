package roomescape.global.auth.repository;

import java.util.Optional;
import roomescape.global.auth.entity.Member;

public interface MemberRepository {

    Member save(Member member);

    Optional<Member> findById(Long id);

    Optional<Member> findByLoginId(String loginId);

}
