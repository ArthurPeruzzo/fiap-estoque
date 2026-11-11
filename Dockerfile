FROM eclipse-temurin:25-jdk-noble AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q
COPY src/ src/
RUN ./mvnw package -DskipTests -q

FROM eclipse-temurin:25-jre-noble
WORKDIR /app

RUN groupadd --gid 10001 estoque \
    && useradd --uid 10001 --gid estoque --no-create-home --no-log-init estoque

COPY --from=build --chown=estoque:estoque /app/target/*.jar app.jar

USER estoque

EXPOSE 8081

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
