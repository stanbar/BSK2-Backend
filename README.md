# Server BSK 2

### Technologies
- **Ktor** - http server framework
- **Kodein** - dependency retrieval container 
- **Shiro** - RBAC security model  
- **SQLite** - lightweight persistence layer
- **ORMLite** - lightweight ORM mapper

### How to run

You can run the service simply by 
``` 
$ ./gradlew run -q
```
or separately build and then run 
```
$ ./gradlew build
$ java -jar build/libs/bsk2-service.jar 
```

### Testing:
- `./testLogin.sh signup <login> <password>`
Send request on /signup for sign up new user.
You can specify 
- *-l or --login*  `<login>` 
- *-p or --password* `<password>`

Optionally you can override  
- *--PESEL* `<PESEL>`
- *--firstName* `<firstName>` 
- *--lastName* `<lastName>`
- *--driverLicence* `<driverLicence>`

**Reponse**
```json
{
  "subject": {
    "login": "stasbar",
    "subjectRoles": [
      {
        "role": {
          "name": "subject_stasbar_1",
          "description": "Subject login: stasbar id: 1 role",
          "permissions": [
            {
              "permission": "subjects:view:1"
            }
          ]
        }
      },
      {
        "role": {
          "name": "carsViewer",
          "description": "The default role given to all users, it allows to view all cars",
          "permissions": [
            {
              "permission": "cars:view:*"
            }
          ]
        }
      }
    ]
  },
  "PESEL": "12345605252",
  "firstName": "stanislaw",
  "lastName": "baranski",
  "driverLicence": "673476254765"
}
```
- `./testLogin.sh login <login> <password>`
Send request on /login for log in 
- *-l or --login*  `<login>` 
- *-p or --password* `<password>`

**Reponse**
```json
{
  "id": 1,
  "subject": {
    "id": 1,
    "login": "stasbar",
    "subjectRoles": [
      {
        "role": {
          "id": 1,
          "name": "subject_stasbar_1",
          "description": "Subject login: stasbar id: 1 role",
          "permissions": [
            {
              "permission": "subjects:read:1"
            },
            {
              "permission": "cars:read:*"
            },
            {
              "permission": "users:read:1"
            }
          ]
        }
      }
    ]
  },
  "PESEL": "12345605252",
  "firstName": "stanislaw",
  "lastName": "baranski",
  "driverLicence": "673476254765"
}
```
- GET /users: `./testLogin.sh users -b {cookie from login response}`

**Reponse**
```json
[
  {
    "id": 1,
    "subject": {
      "id": 1,
      "login": "stasbar",
      "subjectRoles": [
        {
          "role": {
            "id": 1,
            "name": "subject_stasbar_1",
            "description": "Subject login: stasbar id: 1 role",
            "permissions": [
              {
                "permission": "subjects:read:1"
              },
              {
                "permission": "cars:read:*"
              },
              {
                "permission": "users:read:1"
              }
            ]
          }
        }
      ]
    },
    "PESEL": "12345605252",
    "firstName": "jan",
    "lastName": "baranski",
    "driverLicence": "673476254765"
  },
  {
    "id": 2,
    "subject": {
      "id": 2,
      "login": "janbar",
      "subjectRoles": [
        {
          "role": {
            "id": 2,
            "name": "subject_janbar_2",
            "description": "Subject login: janbar id: 2 role",
            "permissions": [
              {
                "permission": "subjects:read:2"
              },
              {
                "permission": "cars:read:*"
              },
              {
                "permission": "users:read:2"
              },
              {
                "permission": "users:read:*"
              }
            ]
          }
        }
      ]
    },
    "PESEL": "12345605252",
    "firstName": "stanislaw",
    "lastName": "baranski",
    "driverLicence": "673476254765"
  }
}
```
- GET /user/{id}: `./testLogin.sh users/1 -b {cookie from login response}`