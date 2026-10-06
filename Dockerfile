FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml checkstyle.xml ./
COPY src ./src
RUN mvn --batch-mode --no-transfer-progress clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/target/bookingsystem-*.jar app.jar
RUN useradd --system --uid 10001 booking
RUN mkdir -p /var/log/bookingsystem \
    && chown -R booking:booking /app /var/log/bookingsystem
USER 10001

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
