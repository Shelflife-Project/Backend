FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /build

# Cache dependencies
COPY pom.xml ./
COPY mvnw .
COPY .mvn .mvn
RUN mvn -B -DskipTests dependency:go-offline || true

# Copy sources and build
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
COPY docker/wait-for-db.sh /usr/local/bin/wait-for-db.sh
RUN chmod +x /usr/local/bin/wait-for-db.sh

EXPOSE 8080

VOLUME ["/app/images"]

ENTRYPOINT ["/usr/local/bin/wait-for-db.sh"]
