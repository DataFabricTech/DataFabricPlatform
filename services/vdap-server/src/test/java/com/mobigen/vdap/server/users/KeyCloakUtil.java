package com.mobigen.vdap.server.users;

import com.mobigen.vdap.server.configurations.AuthConfig;
import com.mobigen.vdap.server.util.JsonUtils;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.userprofile.config.UPConfig;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Slf4j
@Component
@NoArgsConstructor
public class KeyCloakUtil {

    private Keycloak keycloak;

    private Map<String, String> userList = new HashMap<>();

    public void initKeyCloak(AuthConfig config) {
        keycloak = Keycloak.getInstance(config.getUrl(), "master", config.getAdmin(), config.getPassword(), "admin-cli");

        // Create Keycloak Realm
        RealmRepresentation realm = new RealmRepresentation();
        realm.setRealm(config.getRealm());
        realm.setEnabled(true);
        keycloak.realms().create(realm);

        // User Profile 업데이트
        UPConfig userProfile = JsonUtils.readValue(profile, UPConfig.class);
        keycloak.realm(config.getRealm()).users().userProfile().update(userProfile);

        // Create Admin Role
        RoleRepresentation createRole =  new RoleRepresentation();
        createRole.setName("realm-admin");
        createRole.setDescription("admin role");
        keycloak.realm(config.getRealm()).roles().create(createRole);

        RoleRepresentation adminRole = keycloak.realm(config.getRealm()).roles().get("realm-admin").toRepresentation();

        // Create Keycloak User
        // admin user 생성
        UserRepresentation admin = new UserRepresentation();
        admin.setUsername("admin");
        admin.setEmail("admin@mobigen.com");
        admin.setEmailVerified(true);
        admin.setEnabled(true);
        admin.setAttributes(Map.of(
                "displayName", List.of("admin"),
                "description", List.of("admin user")
        ));
        admin.setRealmRoles(List.of("default-roles-" + config.getRealm()));
        Response res = keycloak.realm(config.getRealm()).users().create(admin);
        res.close();
        List<UserRepresentation> users = keycloak.realm(config.getRealm()).users().search("admin").stream().toList();
        if (users.size() == 1) {
            String userId = users.getFirst().getId();
            RoleResource roleResource = keycloak.realm(config.getRealm()).roles().get("realm-admin");
            keycloak.realm(config.getRealm()).users().get(userId).roles().realmLevel().add(List.of(roleResource.toRepresentation()));
        }

        // jblim user 생성
        UserRepresentation jblim = new UserRepresentation();
        jblim.setUsername("jblim");
        jblim.setEmail("jblim@mobigen.com");
        jblim.setEmailVerified(true);
        admin.setEnabled(true);
        jblim.setAttributes(Map.of(
                "displayName", List.of("jblim"),
                "description", List.of("test user jblim")
        ));
        jblim.setRealmRoles(List.of("default-roles-" + config.getRealm()));
        keycloak.realm(config.getRealm()).users().create(jblim).close();

        // test001 생성
        UserRepresentation test001User = new UserRepresentation();
        test001User.setUsername("test001");
        test001User.setEmail("test001@mobigen.com");
        test001User.setEmailVerified(true);
        admin.setEnabled(true);
        test001User.setAttributes(Map.of(
                "displayName", List.of("Test001"),
                "description", List.of("test 001 user")
        ));
        test001User.setRealmRoles(List.of("default-roles-" + config.getRealm()));
        keycloak.realm(config.getRealm()).users().create(test001User).close();

        // test002 생성
        UserRepresentation test002User = new UserRepresentation();
        test002User.setUsername("test002");
        test002User.setEmail("test002@mobigen.com");
        test002User.setEmailVerified(true);
        admin.setEnabled(true);
        test002User.setAttributes(Map.of(
                "displayName", List.of("Test002"),
                "description", List.of("test 002 user")
        ));
        test002User.setRealmRoles(List.of("default-roles-" + config.getRealm()));
        keycloak.realm(config.getRealm()).users().create(test002User).close();

        users = keycloak.realm(config.getRealm()).users().list();
        for(UserRepresentation user : users) {
            userList.put(user.getUsername(), user.getId());
        }
    }

    public void cleanup() {
        keycloak.close();
    }

    private final String profile = "{\n" +
            "  \"attributes\": [\n" +
            "    {\n" +
            "      \"name\": \"username\",\n" +
            "      \"displayName\": \"${username}\",\n" +
            "      \"validations\": {\n" +
            "        \"length\": {\n" +
            "          \"min\": 3,\n" +
            "          \"max\": 255\n" +
            "        },\n" +
            "        \"username-prohibited-characters\": {},\n" +
            "        \"up-username-not-idn-homograph\": {}\n" +
            "      },\n" +
            "      \"permissions\": {\n" +
            "        \"view\": [\n" +
            "          \"admin\",\n" +
            "          \"user\"\n" +
            "        ],\n" +
            "        \"edit\": [\n" +
            "          \"admin\",\n" +
            "          \"user\"\n" +
            "        ]\n" +
            "      },\n" +
            "      \"multivalued\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"email\",\n" +
            "      \"displayName\": \"${email}\",\n" +
            "      \"validations\": {\n" +
            "        \"email\": {},\n" +
            "        \"length\": {\n" +
            "          \"max\": 255\n" +
            "        }\n" +
            "      },\n" +
            "      \"required\": {\n" +
            "        \"roles\": [\n" +
            "          \"user\"\n" +
            "        ]\n" +
            "      },\n" +
            "      \"permissions\": {\n" +
            "        \"view\": [\n" +
            "          \"admin\",\n" +
            "          \"user\"\n" +
            "        ],\n" +
            "        \"edit\": [\n" +
            "          \"admin\",\n" +
            "          \"user\"\n" +
            "        ]\n" +
            "      },\n" +
            "      \"multivalued\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"displayName\",\n" +
            "      \"displayName\": \"${displayName}\",\n" +
            "      \"validations\": {\n" +
            "        \"length\": {\n" +
            "          \"min\": \"0\",\n" +
            "          \"max\": \"255\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"annotations\": {},\n" +
            "      \"permissions\": {\n" +
            "        \"view\": [\n" +
            "          \"admin\",\n" +
            "          \"user\"\n" +
            "        ],\n" +
            "        \"edit\": [\n" +
            "          \"admin\",\n" +
            "          \"user\"\n" +
            "        ]\n" +
            "      },\n" +
            "      \"multivalued\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"description\",\n" +
            "      \"displayName\": \"${description}\",\n" +
            "      \"validations\": {\n" +
            "        \"length\": {\n" +
            "          \"min\": \"0\",\n" +
            "          \"max\": \"4095\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"annotations\": {},\n" +
            "      \"permissions\": {\n" +
            "        \"view\": [\n" +
            "          \"admin\",\n" +
            "          \"user\"\n" +
            "        ],\n" +
            "        \"edit\": [\n" +
            "          \"admin\",\n" +
            "          \"user\"\n" +
            "        ]\n" +
            "      },\n" +
            "      \"multivalued\": false\n" +
            "    }\n" +
            "  ],\n" +
            "  \"groups\": [\n" +
            "    {\n" +
            "      \"name\": \"user-metadata\",\n" +
            "      \"displayHeader\": \"User metadata\",\n" +
            "      \"displayDescription\": \"Attributes, which refer to user metadata\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";


}
