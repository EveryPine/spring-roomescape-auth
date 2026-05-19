package roomescape.global.auth.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.global.auth.entity.Member;

public class FakeMemberRepository implements MemberRepository {

    private final AtomicLong id = new AtomicLong(0);
    private final List<Member> members = new ArrayList<>();

    @Override
    public Member save(Member member) {
        Member savedMember = member.withId(id.addAndGet(1));
        members.add(savedMember);
        return savedMember;
    }

    @Override
    public Optional<Member> findById(Long id) {
        return members.stream()
            .filter(member -> member.getId().equals(id))
            .findFirst();
    }

    @Override
    public Optional<Member> findByLoginId(String loginId) {
        return members.stream()
            .filter(member -> member.getLoginId().equals(loginId))
            .findFirst();
    }
}
