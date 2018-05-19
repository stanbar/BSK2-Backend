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

# Docs:
## POST /signup

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
  "id": 1,
  "subject": {
    "id": 1,
    "login": "stasbar"
  },
  "PESEL": "12345605252",
  "firstName": "stanislaw",
  "lastName": "baranski",
  "driverLicence": "673476254765"
}           
```
## Login
### GET /myRoles
In order to start session you need to provide both Credentials and pick one Role

First of all you should fetch roles assigned to user, to do so you can send request on /myRoles with credentials

`./testLogin.sh myRoles <login> <password>`

you will receive array or roles
```json
[
  {
    "name": "subject_stasbar_4",
    "description": "Default role for each subject, having access to read his data, read all cars, and create rent",
    "id": 4
  },{
    "name": "moderator",
    "description": "Access to read and update users and cars",
    "id": 2
  },{
    "name": "admin",
    "description": "Access to read and update whole domain database",
    "id": 1
  }
]
```

### POST /login
Now you can chose one roleId and login

`./testLogin.sh login -r <roleId> <login> <password>`
Send request on /login
- *-r or --role* `<roleId>`  
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
          "description": "Default role for each subject, having access to read his data, read all cars, and create rent",
          "permissions": [
            { "permission": "subject:read:1" },
            { "permission": "car:read:*" },
            { "permission": "rent:create:*" },
            { "permission": "user:read:1" }
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
You will also get cookie for next requests
`Set-Cookie: SESSIONID=id%3D%2523sc13efc59-7382-4145-ace3-f3eb35d61edd%26roleId%3D%2523l4;`

#### GET /users/{id} 
`./testLogin.sh users/1 -b <cookie from login response>`

```json

{
  "id": 1,
  "subject": {
    "id": 1,
    "login": "stasbar",
    "subjectRoles": [
      {
        "role": {
          "id": 3,
          "name": "subject_stasbar_1",
          "description": "Default role for each subject, having access to read his data, read all cars, and create rent",
          "permissions": [
            {
              "permission": "subject:read:1"
            },
            {
              "permission": "car:read:*"
            },
            {
              "permission": "rent:create:*"
            },
            {
              "permission": "user:read:1"
            }
          ]
        }
      },
      {
        "role": {
          "id": 1,
          "name": "admin",
          "description": "Access to read and update whole domain database",
          "permissions": [
            {
              "permission": "user:*:*"
            },
            {
              "permission": "car:*:*"
            },
            {
              "permission": "rent:*:*"
            },
            {
              "permission": "repair:*:*"
            }
          ]
        }
      },
      {
        "role": {
          "id": 2,
          "name": "moderator",
          "description": "Access to read and update users and cars",
          "permissions": [
            {
              "permission": "user:read:*"
            },
            {
              "permission": "user:update:*"
            },
            {
              "permission": "car:read:*"
            },
            {
              "permission": "car:update:*"
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