# JWT: разграничаване на изтекъл и невалиден токън

Проектът надгражда `upr3` и използва същия съвместим стек: Java EE 8
(`javax.*`), Jersey 2.41, Servlet 3.1 и Jetty 9.4.

## Стартиране от IntelliJ IDEA

Отворете Maven панела и изпълнете `Plugins -> jetty -> jetty:run`, или:

```bash
mvn clean jetty:run
```

Приложението е на `http://localhost:8080/upr6/`, а REST API е на
`http://localhost:8080/upr6/webapi`.

Тестови потребители: `ivan@example.com / ivan123`,
`maria@example.com / maria123`, `student@example.com / student123`.

```bash
curl -i -H 'email: student@example.com' -H 'password: student123' \
  http://localhost:8080/upr6/webapi/authenticate

curl -i -H 'Authorization: Bearer TOKEN' \
  http://localhost:8080/upr6/webapi/favoriteFruits
```

Валиден JWT връща имейла на потребителя. Изтекъл JWT води до HTTP 401,
а невалиден JWT — до HTTP 403. И двата отговора съдържат
`WWW-Authenticate`.
