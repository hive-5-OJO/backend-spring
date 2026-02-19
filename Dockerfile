FROM gradle:8.14.2-jdk17 AS builder
WORKDIR /app
COPY . .

# gradlew가 backend-spring 안에 있으니 거기로 이동해서 실행
WORKDIR /app/backend-spring
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar -x test --no-daemon

RUN ls -al /app/backend-spring/build/libs
RUN jar tf /app/backend-spring/build/libs/*.jar | grep -E "db/migration|application.properties" || true

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/backend-spring/build/libs/*.jar app.jar
ENTRYPOINT ["sh", "-c", "java -jar app.jar"]
