# Server BSK 2
This is backend for 
[BSK2-Frontend](https://github.com/stasbar/BSK2-Frontend)

### Technologies
- **Ktor** - http server framework
- **Kodein** - dependency retrieval container
- **Shiro** - RBAC security model
- **SQLite** - lightweight persistence layer
- **ORMLite** - lightweight ORM mapper

# DB Schema
Domain
![DomainSchema](https://i.imgur.com/YnEQDKt.png)

RBAC
![RBAC](https://i.imgur.com/QecQDUC.png)


Database was bootstrapped with two base roles **admin** and **moderator**

```kotlin
roleService.createRole("admin", "Access to read and modify whole domain database nad read rbac DB",
    listOf(
            Permission.from(USER, ALL),
            Permission.from(CAR, ALL),
            Permission.from(RENT, ALL),
            Permission.from(REPAIR, ALL),
            Permission.from(MECHANIC, ALL),

            Permission.from(ROLE, READ),
            Permission.from(SUBJECT, READ)
    ))

roleService.createRole("moderator", "Access to read all domain DB and modify users and cars",
    listOf(
            Permission.from(USER, ALL),
            Permission.from(CAR, ALL),
            Permission.from(RENT, READ),
            Permission.from(REPAIR, READ),
            Permission.from(MECHANIC, READ)
    )
)
```
Every new User is assigned to default role with READ it's own content, READ all cars and CREATE Rent
```kotlin
val defaultRole = createRole(
        name = "subject_${subject.login}_${subject.id}",
        description = "Default role for each subject, having access to read his data, read all cars, and create rent",
        permissionsStrings = listOf(
                Permission.from(SUBJECT, READ, subject.id),
                Permission.from(CAR, READ),
                Permission.from(RENT, CREATE)
        )
)
```
For testing purposes **stasbar** and **patmil** user was added, with **admin** and **moderator** roles as well as it's own **subject_stasbar_1** default role

```kotlin

val stasbarUser = userService.createUser(login = "stasbar",
        password = "hardpassword",
        firstName = "Stanislaw",
        lastName = "Baranski",
        PESEL = "12345204412",
        driverLicence = "fasdzxvc")
roleService.addRoleToSubject(admin, stasbarUser.subject)
roleService.addRoleToSubject(moderator, stasbarUser.subject)

val patmilUser = userService.createUser(login = "patmil",
        password = "hardpassword",
        firstName = "Patryk",
        lastName = "Milewski",
        PESEL = "12345204412",
        driverLicence = "fasdzxvc")
roleService.addRoleToSubject(moderator,patmilUser.subject)

```

Two qualified mechanics were added
```kotlin
val janusz = mechanicService.createMechanic("januszGka", "Janusz", "Druciarski", "halinaObiad")
val sebix = mechanicService.createMechanic("sebaGda", "Sebastian", "Trytyt", "bedziepanzadowolony")
```

some cars
```kotlin
carService.createCar("Lexus", "IS300", 1234.56)
carService.createCar("VW", "Golf 4", 12.56)
carService.createCar("VW", "Golf 3", 9.99)
carService.createCar("Audi", "A4", 3.99)
carService.createCar("Audi", "A5", 3.99)
val acztery = carService.createCar("Fiat", "Panda", 3.99)
val aczy = carService.createCar("Audi", "A3", 3.99)
```

and two of them need to be repaired
```
repairService.createRepair(aczy, janusz)
repairService.createRepair(acztery, sebix)
```

### How to run

You can run the service simply by 
``` 
$ ./gradlew run -q
```
or separately build and then run 
```
$ ./gradlew shadowJar
$ java -jar build/libs/bsk2-server-all.jar 
```
SSL is enabled by default, in order to generate keystore with Self-Signed Certificate you can use
```
$ ./generateKeystore.sh
```
or better use CA to sign it

Now your server is serving on both `http://localhost:8080` and `https://localhost:8443`



# REST API:
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
            {"permission": "subject:read:1"},
            {"permission": "car:read:*"},
            {"permission": "rent:create:*"},
            {"permission": "user:read:1"}
          ]
        }
      },
      {
        "role": {
          "id": 1,
          "name": "admin",
          "description": "Access to read and update whole domain database",
          "permissions": [
            {"permission": "user:*:*"},
            {"permission": "car:*:*"},
            {"permission": "rent:*:*"},
            {"permission": "repair:*:*"}
          ]
        }
      },
      {
        "role": {
          "id": 2,
          "name": "moderator",
          "description": "Access to read and update users and cars",
          "permissions": [
            {"permission": "user:read:*"},
            {"permission": "user:update:*"},
            {"permission": "car:read:*"},
            {"permission": "car:update:*"}
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

#### GET /cars/{id}
Require *car:read* permission
Return car of ***{id}*** or whole list if not specified

#### GET /mechanics/{id}
Require *mechanic:read* permission
Return mechanic of ***{id}*** or whole list if not specified

#### GET /rents/{id}
Require *rent:read* permission
Return rent of ***{id}*** or whole list if not specified

#### POST /rents
Require *rent:write* permission
Create rent for specific car and peroid of time
required data
```typescript
{
    userId: string,
    carId: string,
    startDate: string,
    endDate: string
}
```

#### GET /repairs/{id}
Require *repair:read* permission
Return repair of ***{id}*** or whole list if not specified

#### POST /repairs/{id}
Require *repair:write* permission
Create repair for specific car
required data
```typescript
{
    mechanicId: string,
    carId: string
}
```


#### GET /subjects/{id}
Require *subject:read* permission
Return subject of ***{id}*** or whole list if not specified

#### GET /roles/{id}
Require *role:read* permission
Return role of ***{id}*** or whole list if not specified


