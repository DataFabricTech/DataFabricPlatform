package com.mobigen.vdap.server.users;

import com.mobigen.vdap.schema.entity.teams.User;
import com.mobigen.vdap.schema.type.EntityReference;
import com.mobigen.vdap.server.Entity;
import com.mobigen.vdap.server.entity.RelationshipEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.mobigen.vdap.common.utils.CommonUtil.nullOrEmpty;
import static com.mobigen.vdap.server.util.EntityUtil.compareEntityReference;

/**
 * 사용자 관련 작업을 관리하는 서비스 클래스입니다.
 * KeyCloakAgent와 상호작용하여 사용자 데이터를 가져오고,
 * 빠른 접근을 위해 로컬 캐시를 유지합니다.
 */
@Slf4j
@Service
public class UserService {
    private final KeyCloakAgent keyCloakAgent;
//    private final Map<String, User> userCacheByEmail;
    private final Map<UUID, User> userCacheByID;

    /**
     * KeyCloakAgent를 사용하여 UserService 인스턴스를 생성합니다.
     * KeyCloakAgent에서 사용자를 가져와 사용자 캐시를 초기화합니다.
     *
     * @param keyCloakAgent 사용자 관리 시스템과 상호작용하는 KeyCloakAgent
     */
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

    /**
     * 제공된 관계 목록을 기반으로 EntityReference 객체 목록을 가져옵니다.
     * 각 관계는 사용자 ID를 사용하여 EntityReference로 변환됩니다.
     * 결과 목록은 정렬된 상태로 반환됩니다.
     *
     * @param relationships 객체 목록
     * @return 정렬된 EntityReference 객체 목록, 입력이 null 또는 비어 있으면 빈 목록 반환
     */
    public List<EntityReference> getReferences(List<RelationshipEntity> relationships) {
        if (nullOrEmpty(relationships)) {
            return Collections.emptyList();
        }
        List<EntityReference> refs = new ArrayList<>();
        for (RelationshipEntity ref : relationships) {
            refs.add(getReferenceById(UUID.fromString(ref.getFromId())));
        }
        refs.sort(compareEntityReference);
        return refs;
    }

    /**
     * UUID를 사용하여 사용자의 EntityReference를 가져옵니다.
     * 사용자가 캐시에 있으면 바로 반환됩니다.
     * 그렇지 않으면 KeyCloakAgent에서 사용자 정보를 가져와 갱신하고 캐시에 추가한 후 반환됩니다.
     *
     * @param id keycloak 으로부터 가져올 사용자의 UUID
     * @return 사용자를 나타내는 EntityReference, 사용자를 찾을 수 없으면 null 반환
     */
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
        // Update Cache And Retry
        cacheUpdate();
        if (userCacheByID.containsKey(id)) {
            User user = userCacheByID.get(id);
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
