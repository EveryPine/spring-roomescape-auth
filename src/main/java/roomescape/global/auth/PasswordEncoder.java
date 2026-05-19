package roomescape.global.auth;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public final class PasswordEncoder {

    public static String encode(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean matches(String password, String encodedPassword) {
        try {
            return BCrypt.checkpw(password, encodedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
