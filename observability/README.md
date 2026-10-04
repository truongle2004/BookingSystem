# Local log observability

The Docker Compose stack writes Spring Boot logs as Logstash JSON, collects them
with Grafana Alloy, stores them in Loki for seven days, and exposes them through
Grafana.

Start the stack:

```shell
docker compose up --build -d
```

Open Grafana at <http://localhost:3000> and sign in with `admin` / `admin` unless
`GRAFANA_ADMIN_USER` and `GRAFANA_ADMIN_PASSWORD` were set. Open the
**Booking System / Booking System Logs** dashboard, or use the preconfigured
Loki data source in **Explore** and query:

```logql
{service_name="bookingsystem"}
```

Useful local endpoints:

- Application: <http://localhost:8080>
- Grafana: <http://localhost:3000>
- Loki readiness: <http://localhost:3100/ready>
- Alloy diagnostics: <http://localhost:12345>

Override `APP_PORT`, `GRAFANA_PORT`, `LOKI_PORT`, or `ALLOY_PORT` if a default
port is already in use. Change the Grafana credentials before exposing this
development stack beyond localhost.
