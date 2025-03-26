package com.mobigen.vdap.server.users;

import com.mobigen.vdap.schema.entity.teams.User;
import com.mobigen.vdap.schema.type.EntityReference;
import com.mobigen.vdap.server.Entity;
import com.mobigen.vdap.server.entity.EntityRelationshipEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.mobigen.vdap.common.utils.CommonUtil.nullOrEmpty;
import static com.mobigen.vdap.server.util.EntityUtil.compareEntityReference;

@Slf4j
@Service
public class UserService {
    private final KeyCloakAgent keyCloakAgent;
//    private final Map<String, User> userCacheByEmail;
    private final Map<UUID, User> userCacheByID;

    public UserService(KeyCloakAgent keyCloakAgent) {
        this.keyCloakAgent = keyCloakAgent;
//        userCacheByEmail = new java.util.HashMap<>();
        userCacheByID = new java.util.HashMap<>();
        cacheUpdate();
    }

    private void cacheUpdate() {
        List<User> users = keyCloakAgent.getUsers();
        if (users != null && !users.isEmpty()) {
            for (User user : users) {
//                if (user.getEmail() != null) {
//                    userCacheByEmail.put(user.getEmail(), user);
//                }
                if (user.getId() != null) {
                    userCacheByID.put(user.getId(), user);
                }
            }
        }
    }

    public List<EntityReference> getReferences(List<EntityRelationshipEntity> relationships) {
        if (nullOrEmpty(relationships)) {
            return Collections.emptyList();
        }
        List<EntityReference> refs = new ArrayList<>();
        for (EntityRelationshipEntity ref : relationships) {
            refs.add(getReferenceById(UUID.fromString(ref.getFromId())));
        }
        refs.sort(compareEntityReference);
        return refs;
    }

    public EntityReference getReferenceById(UUID id) {
        if (userCacheByID.containsKey(id)) {
            User user = userCacheByID.get(id);
            return new EntityReference()
                    .withType(Entity.USER)
                    .withId(user.getId())
                    .withName(user.getName())
                    .withDisplayName(user.getDisplayName())
                    .withDescription(user.getDescription());
        }
        User user = keyCloakAgent.getUserByID(id);
        if (user != null) {
            userCacheByID.put(user.getId(), user);
//            userCacheByEmail.put(user.getEmail(), user);
            return new EntityReference()
                    .withType(Entity.USER)
                    .withId(user.getId())
                    .withName(user.getName())
                    .withDisplayName(user.getDisplayName())
                    .withDescription(user.getDescription());
        }
        return null;
    }
}
