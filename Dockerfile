FROM openjdk:22-ea-22-jdk-slim
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} ldap-navigator-0.0.1-SNAPSHOT.jar

# Make port 8080 available to the world outside this container
EXPOSE 8080

ENTRYPOINT ["java","-jar","/ldap-navigator-0.0.1-SNAPSHOT.jar"]