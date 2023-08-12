FROM openjdk:17 AS builder
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src
RUN chmod +x ./gradlew
RUN microdnf install findutils
RUN ./gradlew build -x test --no-daemon

# base-image
FROM openjdk:17
# build file path
RUN mkdir /opt/app
# copy jar file to container
COPY --from=builder build/libs/*.jar /opt/app/app.jar
# run jar file
ENTRYPOINT ["java","-jar","/opt/app/app.jar","--spring.profiles.active=prod"]
