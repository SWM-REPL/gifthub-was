# base-image
FROM openjdk:17
# build file path
ARG JAR_FILE=build/libs/*.jar
# copy jar file to container
COPY ${JAR_FILE} app.jar
# copy application.properties to container
VOLUME ["./src/main/resources/application.properties", "/src/main/resources/application-prod.properties"]
# run jar file
ENTRYPOINT ["java","-jar","/app.jar", "--spring.profiles.active=prod"]