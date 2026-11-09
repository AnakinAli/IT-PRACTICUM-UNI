# upr9 — Stateful EJB question wizard

Build the WAR with:

```bash
mvn clean package
```

For a quick Servlet/UI run with Jetty:

```bash
mvn jetty:run
```

Then open <http://localhost:8080/upr9/>.

Jetty does not provide an EJB container. The application therefore uses a plain
Java fallback there so that the UI can be exercised, but EJB lifecycle callbacks
and `@StatefulTimeout` are not managed by Jetty. To test `@Remove`, the five-second
timeout and `@PreDestroy` as EJB features, deploy `target/upr9.war` to a full Java
EE 8 server such as WildFly or Payara and watch the server console.
