---
권한 관리 설계 문서
---

# 권한 관리

## 1. 개요

본 문서는 사용자의 접근 권한 관리를 위한 설계 문서로 유스케이스, 인터페이스, 시퀀스, 클래스, 데이터베이스 설계서를 포함한다.

## 2. 요구사항

- 권한 관리 보안 요구사항  
  - 역할 기반 접근 제어(RBAC)
    - 사용자 그룹(예: 관리자, 일반 사용자, 게스트 등)에 따라 접근 권한을 설정해야 한다.
    - 최소 권한 원칙(Principle of Least Privilege, POLP)을 적용해야 한다.
    - 관리자는 시스템 내에서 특정 역할을 가진 사용자만 생성할 수 있어야 한다.
  - 권한 상승 방지
    - 사용자가 임의로 자신의 권한을 변경할 수 없도록 해야 한다.
    - 관리자 계정의 생성 및 수정은 로그로 기록하고, 감사가 가능해야 한다.
  - API 접근 권한 설정
    - API 호출 시 사용자 인증 및 권한 검사를 수행해야 한다.
    - 관리 기능을 수행하는 API는 추가적인 인증(예: API 키, OAuth 2.0 등)을 요구해야 한다.

## 3. Usecase

```plantuml
@startuml authorization_usecase
left to right direction
actor Admin

note left of (Admin)
  AccessControl에 접근 가능한 사용자
end note

package AccessControl {
  usecase "Resource\nManagement" as resource_management
  usecase "Resource\nList" as res_list
  note "서버에서 관리하는 리소스 정보" as res_list_note
  res_list .. res_list_note
  usecase "Resource\nLoad" as res_load
  note "VDAP 서비스 내 마이크로 서비스(외부와 연동하는)들로부터\n리소스 정보를 로드하여 출력" as res_load_note
  res_load .. res_load_note
  usecase "Resource\nAdd" as res_add
  usecase "Resource\nDel" as res_del
  usecase "Resource\nModify" as res_mod
  resource_management <|-- res_list
  resource_management <|-- res_add
  resource_management <|-- res_del
  resource_management <|-- res_mod
  resource_management <|-- res_load
  usecase "Role\nManagement" as role_management
  usecase "Role\nList" as role_list
  usecase "Role\nAdd" as role_add
  usecase "Role\nDel" as role_del
  usecase "Role\nModify" as role_mod
  role_management <|-- role_list
  role_management <|-- role_add
  role_management <|-- role_del
  role_management <|-- role_mod
  usecase "Attribute\nManagement" as attr_management
  usecase "Attribute\nList" as attr_list
  usecase "Attribute\nAdd" as attr_add
  usecase "Attribute\nDel" as attr_del
  usecase "Attribute\nModify" as attr_mod
  attr_management <|-- attr_list
  attr_management <|-- attr_add
  attr_management <|-- attr_del
  attr_management <|-- attr_mod
  note "Role, Attribute 의 경우 화면 내에서 그룹,\n 사용자와 연결할 수 있도록 화면 필요" as relation_note
  role_management .. relation_note
  relation_note .. attr_management
  usecase "Permission\nManagement" as permission_management
  usecase "Permission\nAdd" as permission_add
  usecase "Permission\nDel" as permission_del
  usecase "Permission\nModify" as permission_mod
  permission_management <|-- permission_add
  permission_management <|-- permission_del
  permission_management <|-- permission_mod
}

Admin --> resource_management
Admin --> role_management
Admin --> attr_management
Admin --> permission_management
@enduml
```

## 4. 시퀀스 & 인터페이스

- Resource
  - Load
  - List
  - Add
  - Del
  - Modify

```plantuml
@startuml
actor Admin as admin
participant Server as server
participant MicroService as service

non_member -> server: Join Request 
server -> auth_server : redirect
non_member <-- auth_server : ok

group JoinMember 
    non_member -> auth_server: Request Join Member
    note over auth_server: param check
    opt param error
    non_member <-- auth_server : Error
    end
    auth_server -> email : Send Email
    auth_server <-- email : Success
    non_member <-- auth_server : Success - Request Email Verification
    opt error
    non_member <-- auth_server : Error - Error Message
    end
end

group Email Re-Send
    non_member -> auth_server : Request Re-Send Email
    auth_server -> email : Send Email
    auth_server <-- email : Success
    non_member <- auth_server : Success
    opt error
    non_member <-- auth_server : Error - Error Message
    end
end

group Email Verification
    non_member -> auth_server : Email Verification
    non_member <-- auth_server : Success
    note over auth_server: check
    non_member <-- auth_server : Ok
    opt error
    non_member <-- auth_server : Error(timeover / ...)
    end
end
@enduml
```

- 회원(관리자)
  - 로그인  
  - 아이디 찾기
  - 비밀번호 찾기  
  - 사용자 정보 변경  
  - 세션
    - 세션 조희  
    - 세션(토큰) 갱신
    - 세션 로그아웃
  - 로그아웃  
  - 회원탈퇴  
  
```plantuml
@startuml
actor Member as member
participant Server as server
participant AuthServer as auth_server
member -> server : Login Main
server -> auth_server : Redirect to Auth Server
member <-- auth_server : ok
group Login
    member -> auth_server: Login
    group success
        server <- auth_server: callback(userinfo, token(access, refresh))
        member <- auth_server : LoginSuccess(token)
    end
    group failure
        member <- auth_server : Login Failure
    end
end

group Modify
    member -> auth_server: MyInfo Modify
    group UserInfo
        member -> auth_server: Req Modify UserInfo
        member <-- auth_server: Password
        member -> auth_server: Password
        member <-- auth_server: Success
    end
    group Password
        member -> auth_server: Req Change Password(New, Old)
        member <-- auth_server: Success
    end
end
group Session
    group List
        member -> auth_server: Get Session List
        member <-- auth_server: OK
    end
    group TokenRefresh
        member -> auth_server: Req token
        member <-- auth_server: OK
    end
    group Logout
        member -> auth_server: Logout
        member <-- auth_server: OK
    end
end
group Logout
    member -> auth_server: Logout
    member <-- auth_server: OK
end
group Withdraw
    member -> auth_server: Req Withdraw
    member <-- auth_server: OK and Logout
end
@enduml
```

- 관리자
  - 사용자  
    - 추가  
    - 수정  
    - 삭제  
  - 그룹
    - 추가  
    - 수정  
    - 삭제  
  - 사용자_그룹  
    - 추가  
    - 수정  
    - 삭제  
  - 그룹_그룹
    - 추가  
    - 수정  
    - 삭제  

```plantuml
@startuml
actor Admin as admin
participant Server as server
group UserManagement
admin -> server : Add New User
admin <-- server : OK
admin -> server : Modify(Password) User Info
admin <-- server : OK
admin -> server : Del User
admin <-- server : OK
end

group GroupManagement
admin -> server : Add New Group
admin <-- server : OK
admin -> server : Modify Group
admin <-- server : OK
admin -> server : Del Group
admin <-- server : OK
end

group UserGroup
admin -> server : Add User To Group
admin <-- server : OK
admin -> server : Change Group Of User
admin <-- server : OK
admin -> server : Del User From Group
admin <-- server : OK
end

group GroupGroup
admin -> server : Add Group To Group
admin <-- server : OK
admin -> server : Change Group Of Group
admin <-- server : OK
admin -> server : Del Group From Group
admin <-- server : OK
end
@enduml
```

---

회원가입 / 로그인 / 로그아웃의 경우 인증서버와의 연동에 따라 개발 필요성이 없을 수 있다.
따라서 이 문서에서 인터페이스를 정의하지 않는다.  

> 인증 시스템에서 보안과 관련된 내용 확인 필요  

1. Non Member  
   1. SignUp  
        Reqquest  
        Response  
   2. Email  
        Reqquest  
        Response  
   3. EmailLink...

2. Member
   1. Login
   2. Find ID
   3. Find PW
   4. Modify User Info
   5. Session
      1. List
      2. Refresh
      3. Session Logout
   6. Logout
   7. Withdraw

3. Admin
   1. User Management
   2. Group Management

## 5. 클래스

<img src="image.png" width="300" height="200" alt="클래스 관계 화살표">

| 유형                    | 기호    | 목적                                                                   |
| ----------------------- | ------- | ---------------------------------------------------------------------- |
| 의존성(Association)     | `-->`   | 객체가 다른 객체를 사용함. ( A `-->` B)                                |
| 확장(Inheritance)       | `<\|--` | 계층 구조에서 클래스의 특수화. (부모 `<\|--` 자식)                     |
| 구현(Implementation)    | `<\|..` | 클래스에 의한 인터페이스의 실현. (Interface `<\|..` Class)             |
| 약한 의존성(Dependency) | `..>`   | 더 약한 형태의 의존성. A 클래스 메소스 파라미터로 B를 사용( A `..>` B) |
| 집합(Aggregation)       | `o--`   | 부분이 전체와 독립적으로 존재할 수 있음( 클래스 `o--` 부분 클래스)     |
| 컴포지션(Composition)   | `*--`   | 부분이 전체 없이 존재할 수 없음( 클래스 `*--` 부분 클래스)             |

```plantuml
@startuml
' abstract        abstract
' abstract class  "abstract class"
' annotation      annotation
' circle          circle
' ()              circle_short_form
' class           class
' class           class_stereo  <<stereotype>>
' diamond         diamond
' <>              diamond_short_form
' entity          entity
' enum            enum
' exception       exception
' interface       interface
' metaclass       metaclass
' protocol        protocol
' stereotype      stereotype
' struct          struct

class           User
class           Group
class           Relation
@enduml
```

## 6. 데이터베이스

**User**  

| Column       | Data Type                             | Constraints               | Index | Desc                  |
| ------------ | ------------------------------------- | ------------------------- | :---: | --------------------- |
| `id`         | UUID                                  | PRIMARY KEY               |   v   | 사용자 테이블 PK Key  |
| `email`      | VARCHAR(255)                          | UNIQUE, NOT NULL          |   v   | 사용자 아이디(이메일) |
| `password`   | VARCHAR(255)                          | NOT NULL                  |       | 사용자 암호           |
| `name`       | VARCHAR(255)                          | NOT NULL                  |       | 사용자 이름           |
| `nickname`   | VARCHAR(255)                          | NOT NULL                  |       | 사용자 별칭           |
| `phone`      | VARCHAR(255)                          |                           |       | 사용자 연락처         |
| `status`     | ENUM('Active', 'Inactive', 'Dormant') | DEFAULT 'Inactive'        |       | 사용자 계정 상태      |
| `created_at` | TIMESTAMP                             | NOT NULL                  |       | 사용자 계정 생성일    |
| `updated_at` | TIMESTAMP                             | DEFAULT CURRENT_TIMESTAMP |       | 사용자 계정 수정일    |

---

**Group (그룹)**  

| Column        | Data Type    | Constraints               | Index | Desc        |
| ------------- | ------------ | ------------------------- | :---: | ----------- |
| `id`          | UUID         | PRIMARY KEY               |   v   | 그룹 아이디 |
| `name`        | VARCHAR(255) | UNIQUE, NOT NULL          |   v   | 그룹 이름   |
| `nickname`    | VARCHAR(255) |                           |       |             |
| `description` | TEXT         |                           |       |             |
| `created_at`  | TIMESTAMP    | NOT NULL                  |       |             |
| `updated_at`  | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP |       |             |

---

**GroupUserRelation( 그룹 - 그룹 or 그룹 - 사용자 관계 정보 테이블)**  

| Column       | Data Type             | Constraints                                     | Index |
| ------------ | --------------------- | ----------------------------------------------- | :---: |
| `parents_id` | UUID                  | FOREIGN KEY → `Group(id)`, NOT NULL             |   v   |
| `child_type` | ENUM('Group', 'User') | NOT NULL                                        |   v   |
| `child_id`   | UUID                  | FOREIGN KEY → `Group(id) or User(id)`, NOT NULL |   v   |
| `joined_at`  | TIMESTAMP             | DEFAULT CURRENT_TIMESTAMP                       |       |

---

**Session**  

| Column            | Data Type   | Constraints                                   | Index | Desc                                 |
| ----------------- | ----------- | --------------------------------------------- | :---: | ------------------------------------ |
| `session_id`      | CHAR(64)    | PRIMARY KEY                                   |   v   | 세션 고유 식별자(sha256 해시)        |
| `user_id`         | UUID        | FOREIGN KEY → `User(id)`, NOT NULL            |   v   | 사용자 테이블의 아이디               |
| `access_token`    | TEXT        | NOT NULL                                      |       | 세션 액세스 토큰(JWT 또는 랜덤 토큰) |
| `refresh_token`   | TEXT        | NOT NULL                                      |       | 리프레시 토큰                        |
| `ip_address`      | VARCHAR(45) | NOT NULL                                      |       | 사용자 로그인 IP                     |
| `user_agent`      | TEXT        | NOT NULL                                      |       | 사용자의 브라우저/기기 정보          |
| `created_at`      | DATETIME    | NOT NULL, DEFAULT CURRENT_TIMESTAMP           |       | 세션 생성 시간                       |
| `expires_at`      | DAATTIME    | NOT NULL                                      |       | 세션 만료 시간                       |
| `last_activity`   | DATETIME    | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE |       | 마지막 요청 시간                     |
| `is_active`       | BOOLEAN     | NOT NULL, DEFAULT TRUE                        |       | 세션 활성 여부(로그아웃 FALSE)       |
| `failed_attempts` | INT         | NOT NULL, DEFAULT 0                           |       | 로그인 실패 횟수                     |

---


## 4. Role (역할)

| Column       | Data Type   | Constraints                  | Index |
|-------------|------------|------------------------------|-------|
| `id`        | UUID       | PRIMARY KEY                  | ✅    |
| `name`      | VARCHAR(255) | UNIQUE, NOT NULL            | ✅    |
| `description` | TEXT      |                              |       |

---

## 5. User_Role (사용자 - 역할 관계)

| Column    | Data Type | Constraints                       | Index |
|-----------|----------|-----------------------------------|-------|
| `user_id` | UUID     | FOREIGN KEY → `User(id)`, NOT NULL | ✅    |
| `role_id` | UUID     | FOREIGN KEY → `Role(id)`, NOT NULL | ✅    |

---

## 6. Group_Role (그룹 - 역할 관계)

| Column    | Data Type | Constraints                       | Index |
|-----------|----------|-----------------------------------|-------|
| `group_id` | UUID    | FOREIGN KEY → `Group(id)`, NOT NULL | ✅    |
| `role_id`  | UUID    | FOREIGN KEY → `Role(id)`, NOT NULL | ✅    |

---

## 7. Permissions (권한)

| Column      | Data Type   | Constraints                          | Index |
|------------|------------|--------------------------------------|-------|
| `id`       | UUID       | PRIMARY KEY                          | ✅    |
| `name`     | VARCHAR(255) | UNIQUE, NOT NULL                    | ✅    |
| `resource` | VARCHAR(255) | NOT NULL                            | ✅    |
| `action`   | ENUM('READ', 'WRITE', 'UPDATE', 'DELETE') | NOT NULL |       |

---

## 8. Role_Permission (역할 - 권한 관계)

| Column       | Data Type | Constraints                          | Index |
|-------------|----------|--------------------------------------|-------|
| `role_id`   | UUID     | FOREIGN KEY → `Role(id)`, NOT NULL  | ✅    |
| `permission_id` | UUID  | FOREIGN KEY → `Permissions(id)`, NOT NULL | ✅    |

---

## 9. Attributes (속성)

| Column       | Data Type   | Constraints                          | Index |
|-------------|------------|--------------------------------------|-------|
| `id`        | UUID       | PRIMARY KEY                          | ✅    |
| `name`      | VARCHAR(255) | NOT NULL                            | ✅    |
| `value`     | TEXT       | NOT NULL                            |       |
| `entity_type` | ENUM('User', 'Group', 'Role', 'Resource') | NOT NULL |       |
| `entity_id`  | UUID      | NOT NULL                             | ✅    |

---

## 10. Permission_Attributes (권한 - 속성 관계)

| Column         | Data Type | Constraints                           | Index |
|--------------|----------|---------------------------------------|-------|
| `permission_id` | UUID  | FOREIGN KEY → `Permissions(id)`, NOT NULL | ✅    |
| `attribute_id`  | UUID  | FOREIGN KEY → `Attributes(id)`, NOT NULL | ✅    |

---

## 11. Session (사용자 세션)

| Column       | Data Type   | Constraints                          | Index |
|-------------|------------|--------------------------------------|-------|
| `id`        | UUID       | PRIMARY KEY                          | ✅    |
| `user_id`   | UUID       | FOREIGN KEY → `User(id)`, NOT NULL  | ✅    |
| `device_info` | TEXT     | NOT NULL                            |       |
| `ip_address` | VARCHAR(255) | NOT NULL                            | ✅    |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP            |       |
| `expires_at` | TIMESTAMP | NOT NULL                             | ✅    |

---

## 12. Audit_Log (감사 로그)

| Column        | Data Type   | Constraints                              | Index |
|--------------|------------|------------------------------------------|-------|
| `id`         | UUID       | PRIMARY KEY                              | ✅    |
| `user_id`    | UUID       | FOREIGN KEY → `User(id)`, NULL 가능      | ✅    |
| `session_id` | UUID       | FOREIGN KEY → `Session(id)`, NULL 가능   | ✅    |
| `role_id`    | UUID       | FOREIGN KEY → `Role(id)`, NULL 가능      |       |
| `resource`   | VARCHAR(255) | NOT NULL                                | ✅    |
| `action`     | ENUM('READ', 'WRITE', 'UPDATE', 'DELETE') | NOT NULL |       |
| `status`     | ENUM('SUCCESS', 'ACCESS_DENIED', 'INVALID_SESSION') | NOT NULL | |
| `reason`     | TEXT       | NULL 가능                                |       |
| `timestamp`  | TIMESTAMP  | DEFAULT CURRENT_TIMESTAMP                | ✅    |

---

# 🔹 추가적인 데이터베이스 최적화
1. **Index 적용**
   - 자주 검색되는 `user_id`, `session_id`, `role_id`, `resource` 등에 인덱스 적용
   - `Audit_Log`의 `timestamp`에 인덱스 추가하여 로그 조회 성능 향상

2. **데이터 보존 정책**
   - `Audit_Log` 테이블은 일정 기간(예: 6개월) 후 `archive_audit_log` 테이블로 이전

3. **Partitioning (파티셔닝)**
   - `Audit_Log`을 월별 파티셔닝하여 대용량 데이터 최적화 (`audit_log_2024_02` 등)

4. **Foreign Key 제약 적용**
   - 데이터 무결성을 유지하기 위해 외래 키 설정 (`ON DELETE CASCADE` 옵션 고려 가능)
   - 

# 2. 저장소 가상화 

## 4. Role (역할)

| Column       | Data Type   | Constraints                  | Index |
|-------------|------------|------------------------------|-------|
| `id`        | UUID       | PRIMARY KEY                  | ✅    |
| `name`      | VARCHAR(255) | UNIQUE, NOT NULL            | ✅    |
| `description` | TEXT      |                              |       |

---

## 5. User_Role (사용자 - 역할 관계)

| Column    | Data Type | Constraints                       | Index |
|-----------|----------|-----------------------------------|-------|
| `user_id` | UUID     | FOREIGN KEY → `User(id)`, NOT NULL | ✅    |
| `role_id` | UUID     | FOREIGN KEY → `Role(id)`, NOT NULL | ✅    |

---

## 6. Group_Role (그룹 - 역할 관계)

| Column    | Data Type | Constraints                       | Index |
|-----------|----------|-----------------------------------|-------|
| `group_id` | UUID    | FOREIGN KEY → `Group(id)`, NOT NULL | ✅    |
| `role_id`  | UUID    | FOREIGN KEY → `Role(id)`, NOT NULL | ✅    |

---

## 7. Permissions (권한)

| Column      | Data Type   | Constraints                          | Index |
|------------|------------|--------------------------------------|-------|
| `id`       | UUID       | PRIMARY KEY                          | ✅    |
| `name`     | VARCHAR(255) | UNIQUE, NOT NULL                    | ✅    |
| `resource` | VARCHAR(255) | NOT NULL                            | ✅    |
| `action`   | ENUM('READ', 'WRITE', 'UPDATE', 'DELETE') | NOT NULL |       |

---

## 8. Role_Permission (역할 - 권한 관계)

| Column       | Data Type | Constraints                          | Index |
|-------------|----------|--------------------------------------|-------|
| `role_id`   | UUID     | FOREIGN KEY → `Role(id)`, NOT NULL  | ✅    |
| `permission_id` | UUID  | FOREIGN KEY → `Permissions(id)`, NOT NULL | ✅    |

---

## 9. Attributes (속성)

| Column       | Data Type   | Constraints                          | Index |
|-------------|------------|--------------------------------------|-------|
| `id`        | UUID       | PRIMARY KEY                          | ✅    |
| `name`      | VARCHAR(255) | NOT NULL                            | ✅    |
| `value`     | TEXT       | NOT NULL                            |       |
| `entity_type` | ENUM('User', 'Group', 'Role', 'Resource') | NOT NULL |       |
| `entity_id`  | UUID      | NOT NULL                             | ✅    |

---

## 10. Permission_Attributes (권한 - 속성 관계)

| Column         | Data Type | Constraints                           | Index |
|--------------|----------|---------------------------------------|-------|
| `permission_id` | UUID  | FOREIGN KEY → `Permissions(id)`, NOT NULL | ✅    |
| `attribute_id`  | UUID  | FOREIGN KEY → `Attributes(id)`, NOT NULL | ✅    |

---
