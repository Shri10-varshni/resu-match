FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN apt-get update \
    && apt-get install -y --no-install-recommends maven \
    && mvn -B -DskipTests package \
    && rm -rf /var/lib/apt/lists/*

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN groupadd --system spring \
    && useradd --system --gid spring --home-dir /app spring
COPY --from=build /workspace/target/resumatch-1.0.0.jar /app/app.jar
USER spring:spring
EXPOSE 8081
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
