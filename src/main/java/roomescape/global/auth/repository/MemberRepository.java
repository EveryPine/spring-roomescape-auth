package roomescape.global.auth.repository;

import roomescape.global.auth.entity.Member;

public interface MemberRepository {

    Member save(Member member);

    boolean existsByLoginId(String loginId);

}
