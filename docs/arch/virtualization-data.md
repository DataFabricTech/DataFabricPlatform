# 데이터 가상화

## 1. 개요

본 문서는 데이터 가상화 설계 문서로 유스케이스, 인터페이스, 시퀀스, 클래스, 데이터베이스 설계서를 포함한다.

## 2. 요구사항

일반 요구사항

1. 다양한 형태의 데이터를 위한 메타데이터
2. 다양한 형태의 데이터로부터 메타데이터 수집
3. 표준 메타데이터  
   1. 표준 용어 사전  
   2. 불용어  
4. 데이터 변경을 감지 자동으로 업데이트하는 기능 개발  

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

## 4. 시퀀스 & 인터페이스

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

## 5. 클래스

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

Database, Storage 가 분리되어 있었으나 통합.
UserDefine Driver를 사용할 수 있는 구조로 변경.

**Storage**  
