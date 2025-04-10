
# Keycloak 설치와 설정

## Realm

## Realm settings -> User Profile

Keycloak 에서 관리되는 필드와 기존 사용자 정보 필드를 매핑하기 위해 설정 필요

User 하위 Attributes
- email
  - 이메일 형식일 것
- name
  - 특수문자불가
  - 길이제한
- displayName
  - 길이제한
- description
  - 길이제한

다음은 User Profile 설정을 위한 JSON 파일이다.

```json
{
  "attributes": [
    {
      "name": "username",
      "displayName": "${username}",
      "validations": {
        "length": {
          "min": 3,
          "max": 255
        },
        "username-prohibited-characters": {},
        "up-username-not-idn-homograph": {}
      },
      "permissions": {
        "view": [
          "admin",
          "user"
        ],
        "edit": [
          "admin",
          "user"
        ]
      },
      "multivalued": false
    },
    {
      "name": "email",
      "displayName": "${email}",
      "validations": {
        "email": {},
        "length": {
          "max": 255
        }
      },
      "required": {
        "roles": [
          "user"
        ]
      },
      "permissions": {
        "view": [
          "admin",
          "user"
        ],
        "edit": [
          "admin",
          "user"
        ]
      },
      "multivalued": false
    },
    {
      "name": "displayName",
      "displayName": "${displayName}",
      "validations": {
        "length": {
          "min": "0",
          "max": "255"
        }
      },
      "annotations": {},
      "permissions": {
        "view": [
          "admin",
          "user"
        ],
        "edit": [
          "admin",
          "user"
        ]
      },
      "multivalued": false
    },
    {
      "name": "description",
      "displayName": "${description}",
      "validations": {
        "length": {
          "min": "0",
          "max": "4095"
        }
      },
      "annotations": {},
      "permissions": {
        "view": [
          "admin",
          "user"
        ],
        "edit": [
          "admin",
          "user"
        ]
      },
      "multivalued": false
    }
  ],
  "groups": [
    {
      "name": "user-metadata",
      "displayHeader": "User metadata",
      "displayDescription": "Attributes, which refer to user metadata"
    }
  ]
}
```

## Fabric Server 설정

- Keycloak URL
- Realm
- Admin Password