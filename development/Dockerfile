FROM openjdk:17 AS builder
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src
RUN chmod +x ./gradlew
RUN microdnf install findutils
RUN ./gradlew build -x test

# base-image
FROM openjdk:17
# build file path
RUN mkdir /opt/app
# copy jar file to container
COPY --from=builder build/libs/*.jar /opt/app/spring-boot-application.jar
EXPOSE 8080
# run jar file
ENTRYPOINT ["java","-jar","/opt/app/spring-boot-application.jar"]
