FROM gradle:9.2.1-jdk21-alpine AS build

WORKDIR /app

COPY gradle gradle
COPY gradlew .
COPY settings.gradle .
COPY build.gradle .

# зависимости
RUN gradle dependencies --no-daemon

# исходный код
COPY src src

RUN gradle build --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
