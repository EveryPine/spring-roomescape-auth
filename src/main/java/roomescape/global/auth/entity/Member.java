package roomescape.global.auth.entity;

public class Member {

    private final Long id;
    private final String name;
    private final String loginId;
    private final String password;
    private final Role role;

    private Member(Long id, String name, String loginId, String password, Role role) {
        this.id = id;
        this.name = name;
        this.loginId = loginId;
        this.password = password;
        this.role = role;
    }


    public static Member create(String name, String loginId, String password, Role role) {
        return new Member(null, name, loginId, password, role);
    }

    public Member withId(Long id) {
        return new Member(id, this.name, this.loginId, this.password, this.role);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }
}
