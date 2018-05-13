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
- /signup:`./testLogin.sh signup stasbar hardpassword`
- /login:`./testLogin.sh login stasbar hardpassword`
- GET /users: `./testLogin.sh users -b {cookie from login response}`
- GET /user/{id}: `./testLogin.sh users/1 -b {cookie from login response}`