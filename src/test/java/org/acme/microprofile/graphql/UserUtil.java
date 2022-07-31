package org.acme.microprofile.graphql;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;

import javax.enterprise.context.ApplicationScoped;
import java.util.Map;

@ApplicationScoped
public class UserUtil {

    @ConfigProperty(name = "quarkus.security.users.embedded.users")
    Map<String, String> users;

    public User getSomeUserEntry() {
        var userOpt = users.entrySet().stream().findAny();
        Assertions.assertTrue(userOpt.isPresent());
        var entry = userOpt.get();
        return new User(entry.getKey(), entry.getValue());
    }

    public static class User {
        final String userName;
        final String password;

        public User(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }
    }
}
