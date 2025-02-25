FROM openjdk:21-jdk-slim AS development
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# 运行环境
FROM openjdk:21-jre-slim AS production
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# 添加环境变量
ENV JAVA_OPTS="-Xmx512m"
ENV SPRING_PROFILES_ACTIVE="prod"

# 暴露端口
EXPOSE 8081

# 启动命令
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]