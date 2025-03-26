package com.mobigen.vdap.server.users;

import com.mobigen.vdap.server.configurations.AuthConfig;
import com.mobigen.vdap.server.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.userprofile.config.UPConfig;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Slf4j
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KeyCloakAgentTest {

    private KeyCloakAgent agent;

    @Container
    public GenericContainer<?> keycloakContainer = new GenericContainer<>(DockerImageName.parse("quay.io/keycloak/keycloak:26.0.7"))
            .withExposedPorts(8080)
            .withEnv("KC_BOOTSTRAP_ADMIN_USERNAME", "admin")
            .withEnv("KC_BOOTSTRAP_ADMIN_PASSWORD", "Mobigen.07$")
            .withCommand("start-dev");

    @BeforeAll
    void init() {
        // start Keycloak container
        keycloakContainer.start();
        // wait for Keycloak to be ready
        keycloakContainer.waitingFor(Wait.forHttp("/").forStatusCode(200));

        AuthConfig config;
        config = new AuthConfig();
        config.setUrl("http://" + keycloakContainer.getHost() + ":" + keycloakContainer.getMappedPort(8080));
        config.setAdmin("admin");
        config.setPassword("Mobigen.07$");
        config.setRealm("ovp");
        agent = new KeyCloakAgent(config);

        // Create Keycloak Realm
        RealmRepresentation realm = new RealmRepresentation();
        realm.setRealm(config.getRealm());
        realm.setEnabled(true);
        agent.getKeycloak().realms().create(realm);

        // User Profile 업데이트가 필요한데...
        UPConfig userProfile = JsonUtils.readValue(profile, UPConfig.class);
        agent.getKeycloak().realm(config.getRealm()).users().userProfile().update(userProfile);

        // Create Keycloak Test User
        UserRepresentation user = new UserRepresentation();
        user.setUsername("");
    }

    @AfterAll
    void cleanUp() {
        agent.cleanUp();
        keycloakContainer.stop();
    }

    @Test
    void getUserByEmail() {
        agent.getUsers();
    }

    @Test
    void getUserByID() {
    }

    @Test
    void getUsers() {
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