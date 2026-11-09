# JWT protected web services

## Стартиране

```bash
mvn clean package
mvn jetty:run
```

Приложението е на `http://localhost:8080/upr3/`, а REST API е на `http://localhost:8080/upr3/webapi`.

Тестови потребители: `ivan@example.com / ivan123`, `maria@example.com / maria123`, `student@example.com / student123`.

```bash
# GET login
curl -H 'email: ivan@example.com' -H 'password: ivan123' http://localhost:8080/upr3/webapi/authenticate

# POST login
curl -X POST -H 'Content-Type: application/json' -d '{"email":"ivan@example.com","password":"ivan123"}' http://localhost:8080/upr3/webapi/authenticate

# След като запишете резултата в TOKEN:
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/upr3/webapi/favoriteFruits
curl -X POST -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"fruit":"манго"}' http://localhost:8080/upr3/webapi/favoriteFruits
curl -X PUT -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"oldFruit":"банан","newFruit":"ананас"}' http://localhost:8080/upr3/webapi/favoriteFruits
curl -X DELETE -H "Authorization: Bearer $TOKEN" http://localhost:8080/upr3/webapi/favoriteFruits/%D1%8F%D0%B1%D1%8A%D0%BB%D0%BA%D0%B0
curl -X POST -H "Authorization: Bearer $TOKEN" http://localhost:8080/upr3/webapi/authenticate/logout
```

JWT може да се подава и чрез заглавка `jwt: <token>`. Токените изтичат след 60 секунди.
