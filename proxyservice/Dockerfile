FROM eclipse-temurin:17.0.10_7-jdk

COPY target/*.jar proxyservice-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/proxyservice-0.0.1-SNAPSHOT.jar"]