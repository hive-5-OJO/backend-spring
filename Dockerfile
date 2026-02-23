# ---------- build stage ----------
FROM gradle:8.14.2-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle clean bootJar -x test --no-daemon

# ---------- run stage ----------
# ubuntu 기반으로 파이썬 연동을 위해 jammy 추가
FROM eclipse-temurin:17-jre-jammy 
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

ENV JAVA_OPTS="-Xms512M -Xmx512M"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"] 

# ---------- aws version ----------
# FROM eclipse-temurin:17-jre-jammy 
# WORKDIR /app

# COPY build/libs/*.jar app.jar

# ENV JAVA_OPTS="-Xms512M -Xmx512M"

# EXPOSE 8080
# ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]