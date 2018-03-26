# Server BSK 2

### Technologies
- **Ktor** - http server framework 
- **Shiro** - RBAC security model  
- **SQLite** - lightweight persistence layer in file
- **Kodein** - dependency retrieval container

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
- get /users: `./testLogin.sh users -b {cookie from login response}`