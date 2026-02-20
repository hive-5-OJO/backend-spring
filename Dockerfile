FROM gradle:8.14.2-jdk17 AS builder
WORKDIR /app
COPY . .

# wrapper 위치가 루트일 수도/백엔드 폴더일 수도 있어서 둘 다 처리
RUN if [ -f "./gradlew" ]; then chmod +x ./gradlew; fi
RUN if [ -f "./backend-spring/gradlew" ]; then chmod +x ./backend-spring/gradlew; fi

# gradlew가 backend-spring에 있으면 기존 방식대로 실행
RUN if [ -f "./gradlew" ]; then \
      ./gradlew -p backend-spring clean bootJar -x test --no-daemon --stacktrace --info; \
    else \
      cd backend-spring && ./gradlew clean bootJar -x test --no-daemon --stacktrace --info; \
    fi


RUN ls -al /app/backend-spring/build/libs
RUN jar tf /app/backend-spring/build/libs/*.jar | grep -E "db/migration|application.properties" || true

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/backend-spring/build/libs/*.jar app.jar
ENTRYPOINT ["sh", "-c", "java -jar app.jar"]
