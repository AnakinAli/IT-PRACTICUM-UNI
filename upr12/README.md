# upr12 — JPA офиси

Проектът е Java EE 8 WAR приложение с JSF, EJB, JPA/Hibernate, TomEE и MySQL 5.7.

## Стартиране на цялото приложение

TomEE Maven plugin е включен в `pom.xml`. Най-лесният начин за стартиране на
сървъра и базата заедно е:

```bash
docker compose up server
```

След като TomEE изпише, че приложението е deploy-нато, то е достъпно на:

<http://localhost:8080/upr12/>

За спиране натиснете `Ctrl+C`, след което може да изпълните:

```bash
docker compose down
```

## База данни

Стартиране:

```bash
docker compose up -d mysql
```

Настройки за връзка:

- адрес: `localhost:3306`
- база: `upr12`
- потребител: `upr12`
- парола: `upr12pass`
- root парола: `rootpass`

Спиране на контейнера:

```bash
docker compose down
```

Данните се пазят в Docker volume `upr12_mysql_data`. За да бъдат изтрити и те,
може изрично да се изпълни `docker compose down -v`.

## Компилиране

При локално инсталирани Java 11 и Maven:

```bash
mvn package
```

Или чрез включения Maven контейнер:

```bash
docker compose run --rm maven mvn package
```

Готовият файл е `target/upr12.war`. При първото стартиране Hibernate създава
таблиците `Office` и `office_addresses`, а `Database` добавя началните данни.
Началното зареждане е идемпотентно и не създава дублирани градове при рестарт.
