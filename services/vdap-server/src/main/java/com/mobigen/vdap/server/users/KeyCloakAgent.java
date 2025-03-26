package com.mobigen.vdap.server.users;

import com.google.common.annotations.VisibleForTesting;
import com.mobigen.vdap.schema.entity.teams.User;
import com.mobigen.vdap.server.configurations.AuthConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class KeyCloakAgent {
    private final AuthConfig authConfig;

    @Getter(onMethod_ = {@VisibleForTesting}) // Lombok을 사용하여 테스트 환경에서만 접근 가능
    private final Keycloak keycloak;

    public KeyCloakAgent(AuthConfig authConfig) {
        this.authConfig = authConfig;
        this.keycloak = Keycloak.getInstance(
                authConfig.getUrl(),
                "master",
                authConfig.getAdmin(),
                authConfig.getPassword(),
                "admin-cli"
        );
    }

    public User getUserByEmail(String email) {
        log.info("[KeyCloakClient] getUserByEmail[{}]", email);
        List<UserRepresentation> users = keycloak.realm(authConfig.getRealm()).users().searchByEmail(email, true);
        if (users == null || users.isEmpty()) {
            log.info("[KeyCloakClient] Not Found User From e-mail[{}]", email);
            return null;
        }
        for (UserRepresentation user : users) {
            if (user.getEmail() != null && user.getEmail().equals(email)) {
                log.info("[KeyCloakClient] Find User. ID[{}] E-Mail[{}] Name[{}]", user.getId(), user.getEmail(), user.getUsername());
                return convertToUser(user);
            }
        }
        return null;
    }

    public User getUserByID(UUID id) {
        log.info("[KeyCloakClient] getUserByID[{}]", id.toString());
        List<User> users = getUsers();
        if (users == null || users.isEmpty()) {
            log.info("[KeyCloakClient] Not Found User From ID[{}]", id);
            return null;
        }
        for (User user : users) {
            if (user.getId() != null && user.getId().equals(id)) {
                log.info("[KeyCloakClient] Find User. ID[{}] E-Mail[{}] name[{}]", user.getId(), user.getEmail(), user.getName());
                return user;
            }
        }
        return null;
    }


    public List<User> getUsers() {
        log.info("[KyeCloakClient] getUsers");
        List<User> users = new ArrayList<>();
        for (UserRepresentation user : keycloak.realm(authConfig.getRealm()).users().list()) {
            log.info("[KeyCloakClient] User Info. ID[{}] E-Mail[{}] Name[{}] DisplayName[{}] Description[{}]",
                    user.getId(), user.getEmail(), user.getUsername(),
                    user.getAttributes().get("displayName").toString(), user.getAttributes().get("description").toString());
            users.add(convertToUser(user));
        }
        return users;
    }

    private User convertToUser(UserRepresentation user) {
        return new User()
                .withId(UUID.fromString(user.getId()))
                .withEmail(user.getEmail())
                .withName(user.getUsername())
                .withDisplayName(user.getAttributes().get("displayName").toString())
                .withDescription(user.getAttributes().get("description").toString());
    }

    public void cleanUp() {
        keycloak.close();
    }

}
