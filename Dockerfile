# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle

RUN chmod +x gradlew
RUN --mount=type=cache,target=/root/.gradle ./gradlew dependencies --no-daemon

COPY src ./src
RUN --mount=type=cache,target=/root/.gradle ./gradlew bootJar -x test --no-daemon

FROM gcr.io/distroless/java17-debian12:nonroot AS runtime-base
WORKDIR /app

ENV PORT=8080
EXPOSE 8080
USER nonroot:nonroot
ENTRYPOINT ["java","-jar","/app/app.jar"]

FROM runtime-base AS runtime-prebuilt
COPY --chown=nonroot:nonroot build/libs/aim-be.jar /app/app.jar

FROM runtime-base AS runtime
COPY --from=build --chown=nonroot:nonroot /workspace/build/libs/aim-be.jar /app/app.jar
