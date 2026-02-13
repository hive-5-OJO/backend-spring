# ---------- build stage ----------
# FROM gradle:8.14.2-jdk17 AS builder
# WORKDIR /app
# COPY . .
# RUN gradle clean bootJar -x test --no-daemon

# ---------- run stage ----------
# ubuntu 기반으로 파이썬 연동을 위해 jammy 추가
FROM eclipse-temurin:17-jre-jammy 
WORKDIR /app
# COPY --from=builder /app/build/libs/*.jar app.jar # build stage 사용시
COPY build/libs/*.jar app.jar

# 컨테이너 실행 최적화 설정 - EC2 1대에 여러 컨테이너를 띄우기에 메모리(Heap) 제한
ENV JAVA_OPTS="-Xms512M -Xmx512M"

EXPOSE 8080
# ENTRYPOINT ["java", "-jar", "app.jar"] # build stage 사용시
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]