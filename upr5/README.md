# JWT authentication with refresh token

Проектът е Maven WAR с Jersey (`javax.ws.rs`) и Jetty 9. Реализира 5-минутен
автентикационен токън, едночасов refresh токън, Bearer синтаксис и
`WWW-Authenticate` отговор при невалиден/изтекъл токън.

## Стартиране през IDE с Jetty

От Maven панела стартирайте целта `jetty:run`, или от терминал:

```bash
mvn clean jetty:run
```

Приложението е на `http://localhost:8080/upr5/`, а API адресът е
`http://localhost:8080/upr5/api`.

Демонстрационните данни са `student@example.com` / `student`. За реален проект
заменете `Authenticate.credentialsAreValid` с проверка в база данни и задайте
лична стойност от поне 64 байта в environment променливата `JWT_SECRET`.

## Примери

Вход:

```bash
curl -i -X POST http://localhost:8080/upr5/api/authenticate \
  -H 'Content-Type: application/json' \
  -d '{"email":"student@example.com","password":"student"}'
```

Отговорът е JSON с ключове `auth` и `refr`. Защитен ресурс:

```bash
curl -i http://localhost:8080/upr5/api/protected \
  -H 'Authorization: Bearer AUTH_TOKEN'
```

Получаване на нов автентикационен токън чрез стойността от `refr`:

```bash
curl -i -X POST http://localhost:8080/upr5/api/refreshAuthToken \
  -H 'Authorization: Bearer REFRESH_TOKEN'
```
