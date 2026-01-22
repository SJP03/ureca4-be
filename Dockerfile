# ---- build stage ----
FROM gradle:8.7-jdk17 AS builder
WORKDIR /workspace
COPY . .
ARG MODULE
RUN gradle :${MODULE}:bootJar -x test

# ---- runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
ARG MODULE
COPY --from=builder /workspace/${MODULE}/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
