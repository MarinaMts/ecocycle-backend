# ================================
# EcoCycle Backend - Dockerfile (deploy no Render)
# Build multi-stage: compila com Maven, roda com JRE enxuto.
# ================================

# ---- Etapa 1: build ----
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copia primeiro so o pom.xml para aproveitar cache de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o restante do codigo e builda (pula os testes - ja rodam no CI/local)
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- Etapa 2: runtime ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# O Render injeta a variavel PORT dinamicamente - a aplicacao precisa escutar nela
# (ver server.port=${PORT:8080} em application-prod.properties)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
