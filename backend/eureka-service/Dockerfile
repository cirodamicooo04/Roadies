FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
RUN apk add --no-cache curl
COPY target/*.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]