# 1. Usamos una imagen con Maven y Java 17 para compilar el proyecto en la nube
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2. Tomamos el archivo compilado (.jar) y lo preparanos para correr en un servidor liviano
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
