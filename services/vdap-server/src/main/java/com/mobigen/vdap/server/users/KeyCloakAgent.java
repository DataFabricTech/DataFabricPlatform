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

/**
 * KeyCloakAgent 클래스는 Keycloak 서버와 상호작용하기 위한 인터페이스를 제공합니다.
 * 이메일 또는 ID를 통해 사용자 정보를 가져오거나, 모든 사용자를 조회하며,
 * Keycloak의 UserRepresentation 객체를 커스텀 User 객체로 변환하는 기능을 제공합니다.
 * 
 * <p>이 클래스는 Spring Component로 정의되어 있으며, Lombok을 사용하여 로깅 및 테스트 유틸리티를 제공합니다.
 * AuthConfig 객체를 통해 Keycloak 연결 정보를 설정합니다.</p>
 * 
 * <p>주요 기능:</p>
 * <ul>
 *   <li>{@link #getUserByEmail(String)}를 사용하여 이메일로 사용자 조회</li>
 *   <li>{@link #getUserByID(UUID)}를 사용하여 UUID로 사용자 조회</li>
 *   <li>{@link #getUsers()}를 사용하여 Realm의 모든 사용자 조회</li>
 *   <li>Keycloak UserRepresentation 객체를 커스텀 User 객체로 변환</li>
 *   <li>{@link #cleanUp()}를 사용하여 Keycloak 클라이언트 리소스 정리</li>
 * </ul>
 * 
 * <p>참고: Keycloak 클라이언트는 제공된 AuthConfig 세부 정보를 사용하여 초기화되며,
 * "master" Realm에 관리자 자격 증명으로 연결됩니다.</p>
 * 
 * <p>사용 예:</p>
 * <pre>
 * {@code
 * AuthConfig authConfig = new AuthConfig(...);
 * KeyCloakAgent keyCloakAgent = new KeyCloakAgent(authConfig);
 * User user = keyCloakAgent.getUserByEmail("example@example.com");
 * }
 * </pre>
 * 
 * <p>의존성:</p>
 * <ul>
 *   <li>로깅 및 테스트 유틸리티를 위한 Lombok 어노테이션</li>
 *   <li>Keycloak 서버와 상호작용하기 위한 Keycloak Admin Client</li>
 * </ul>
 * 
 * <p>KeyCloakAgent 인스턴스가 더 이상 필요하지 않을 때 {@link #cleanUp()}를 호출하여
 * 리소스를 해제하는 것을 잊지 마십시오.</p>
 */
@Slf4j
@Component
public class KeyCloakAgent {
    private final AuthConfig authConfig;

    // Lombok 을 사용하여 테스트 환경에서만 접근 가능
    @Getter(onMethod_ = {@VisibleForTesting})
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
                log.info("[KeyCloakClient] Find User. ID[{}] E-Mail[{}] Name[{}]", user.getId(), user.getEmail(), user.getName());
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
