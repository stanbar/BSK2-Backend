# Server BSK 2

Technologies
- Ktor - http server framework 
- Shiro - RBAC security model  
- SQLite - lightweight persistence layer in file

###Testing:
- /signup:`./testLogin.sh signup stasbar hardpassword`
- /login:`./testLogin.sh login stasbar hardpassword`
- get /users: `./testLogin.sh users -b {cookie from response here}`