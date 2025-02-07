# 검색

## 1. 개요

본 문서는 데이터 검색(탐색)을 위한 설계 문서로 유스케이스, 인터페이스, 시퀀스, 클래스, 데이터베이스 설계서를 포함한다.

## 2. 요구사항

자연어 검색  
검색 기록  
검색 추천  
검색 클러스터 구성  

- 저장소 탐색
  - 저장소 리스트  
  - 저장소 정보 열람  
  - 즐겨찾기
  - 카테고리  
  - 사전  
  - 태그  

추상화 된 데이터 스키마의 메타 정보를 이용하여 검색 기능 개발
추상화된 데이터의 원본 데이터를 샘플링하여 제공
데이터 검색 수행 시 클러스터링된 검색엔진에서 검색에 필요한 다중 저장소에 동시에 접근하여 검색 결과에 대한 비동기 조합으로 성능 최적화

- 보안
  - 접근 제어(RBAC, Attribute(User, Group))
    - 초기 컨셉은 최초 모두 검색 가능하고, 특이 행동에 대해서 접근제어를 수행하고자 하였었음.

### 2.1. Rate Limiting

사용자 관련 API에서 Rate Limit(속도 제한) 을 적용해야 하는 주요 부분

(1) 게시글 작성/댓글 API (스팸 방지)  
예: POST /api/posts 또는 POST /api/comments  
이유:  
자동화된 스팸 댓글, 도배 방지  
추천 제한:  
동일 IP 또는 계정에서 10~20회/분 이상 요청 시 제한  

(2) 검색 API (DDoS 방지)  
예: GET /api/search?q=...  
이유:  
과도한 검색 요청으로 인한 서버 부하 방지  
추천 제한:  
동일 IP에서 10~20회/분 이상 요청 시 제한  

## 3. Usecase

```plantuml
@startuml vdap_usecase
allowmixing
left to right direction
actor Nonmember
actor Member
actor Admin

package NonMemberFunc{
  usecase "Join the\nMembership" as join
  usecase "Email Verification" as email_verification
  email_verification -right-|> join
}

package MemberFunc{
  usecase "Login" as login
  usecase "Find ID" as find_id
  usecase "Find Password" as find_pw
  usecase "Modify\nUserInfo" as modify_user_info
  usecase "Logout" as logout
  usecase "Withdraw\nMembership" as withdraw
}

package AdminFunc{
  usecase "User\nManagement" as user_management
  usecase "UserAdd" as user_add
  usecase "UserDel" as user_del
  usecase "UserPasswordSet" as user_password_set
  user_management <|-down- user_add
  user_management <|-down- user_del
  user_management <|-down- user_password_set 
  usecase "Group\nManagement" as group_management
  usecase "GroupAdd" as group_add
  usecase "GroupDel" as group_del
  usecase "Group\nModify" as group_modify
  group_management <|-- group_add 
  group_management <|-- group_del
  group_management <|-- group_modify
  usecase "Group\nRelation" as group_relation
  usecase "GroupAddToGroup" as group_add_group
  usecase "GroupDelFromFroup" as group_del_group
  usecase "UserAddToGroup" as user_add_group
  usecase "UserDelFromGroup" as user_del_group
  group_relation <|-- group_add_group
  group_relation <|-- group_del_group
  group_relation <|-- user_add_group
  group_relation <|-- user_del_group
}

Nonmember --> join

Member --> login
Member --> find_id
Member --> find_pw
Member --> logout
Member --> modify_user_info
Member --> withdraw

Member <|-- Admin

Admin --> user_management
Admin --> group_management
Admin --> group_relation

@enduml
```

## 3. 시퀀스 & 인터페이스

- 비회원  
  - 회원가입  
  - 이메일인증요청  

```plantuml
@startuml
Actor User as user
box "OpenVDAP Service" #Lightblue
participant Server as server
participant Metadata as metadata
participant Monitoring as monitoring
database Database as database
database ElasticSearch as search 
end box

user -> server ++ : StorageList
user <- server -- : Res : []ResStorageInfo
|||
user -> server ++ : Get Storage(getbyid, getbyname)
server -> database ++ : Get Storage Info
server <-- database --: Res
user <-- server --: Res : ResStorageInfo
actor NonMember as non_member
participant Server as server
participant AuthServer as auth_server
participant EmailServer as email

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

- UserTable

| Field | DataType | Constraint | Default | Desc |
| ----- | -------- | ------- | ------- | ---- |
| id    | UUID     |         |         |      |

json User {
  "id": "uuid",
  "email": "email address",
  "pw": "password",
  "name": "name",
  "nickname": "nickname",
  "phone": "phone",
  "status": ["active", "inactive", "etc"],
  "createdAt": "create time",
  "updatedAt": "update time"
}

- GroupTable

- UserGroupRelation


json Group {
  "id": "uuid",
  "name": "name",
  "nickname": "nickname",
  "desc": "description",
  "createdAt": "create time",
  "updatedAt": "update time"
}

json UserGroupRelation {
  "parentID": "group id",
  "childType": ["group", "user"],
  "childID": ["group id", "user id"],
  "joined_at": "join time"
}

json SignUpReq {
  "id": "email address",
  "pw": "password",
  "name": "name",
  "nickname": "nickname",
  "phone": "phone",
  "etc": "etc"
}

json SignUpRes {
  "code": ["success", "error"],
  "errMsg": "error message",
  "body": {
    "message": "User registered successfully"
  }
}

SignUpReq --> SignUpRes

note "SignUp 완료 후 이메일 전송과 이메일 내 링크 연결을 통해 이메일 인증을 완료" as signup_note

SignUpReq .. signup_note

json LoginReq {
  "id": "user@example.com",
  "password": "securepassword",
  "device_info": "Chrome - Windows"
	' Device Info: 사용자가 로그인한 기기 정보를 DB에 저장
}

note "다중 세션을 위해 추가적인 정보 `device_info`를 사용(예시)" as login_note
LoginReq .. login_note

json LoginRes {
  "access_token": "eyJhbGciOiJIUzI1...",
  "refresh_token": "d1f13c1c-bc87...",
  "expires_in": 3600
  ' Access Token: 짧은 수명의 JWT (예: 1시간)
	' Refresh Token: 기기별 고유한 UUID 기반 토큰 (DB 또는 Redis에 저장)
}
LoginReq -> LoginRes

' 이메일 인증이 완료되지 않은 사용자의 경우 이메일 인증 요청 화면으로 이동
json EmailVerificationReq {
  "request": "..."
}

' 이메일 인증은 5분 내로 완료되지 못하는 경우 인증 링크로 접속하더라도 인증 실패 

json TokenRefreshReq {
  "refresh_token": "d1f13c1c-bc87..."
}

json TokenRefreshRes {
  "access_token": "new-access-token",
  "expires_in": 3600
}

note "Refresh Token이 유효하지 않으면 로그아웃 처리" as token_refresh_note
TokenRefreshReq .. token_refresh_note

TokenRefreshReq --> TokenRefreshRes

' 세션 테이블은 Audit Log와 연결된다.
json sessionTable {
  "id": "session id", 
  "userId": "userId",
  "refresh_token": "token...",
  "device_info": "device info",
  "status": ["active", "inactive"],
  "createdAt": "create time",
  "ipAddress": "IP Address", 
  "expiresAt": "token 만료 시간"
}

note "현재 접속된 기기 정보 조회" as session_list_note

json SessionList {
  "SessionList": [
    {
      "device_info": "Chrome - Windows",
      "last_active": "2024-02-03T12:00:00Z"
    },
    {
      "device_info": "Safari - iPhone",
      "last_active": "2024-02-02T23:45:00Z"
    }
  ]
}

SessionList .. session_list_note

' 1.	Access Token은 짧은 만료 시간 (1시간 이하)
' 2.	Refresh Token은 DB 또는 Redis에 저장 (기기별 관리)
' 3.	로그아웃 시 Refresh Token을 삭제하여 세션 종료
' 4.	Blacklist를 활용하여 강제 로그아웃 가능
' 5.	JWT 서명키는 안전하게 관리 (환경 변수 사용)
' 6.	Rate Limiting 적용 (특히 로그인, 토큰 갱신 API에 적용)" as security_note


# 2. 저장소 가상화 