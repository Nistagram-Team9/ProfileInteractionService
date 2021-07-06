FROM maven:3.6.3-ibmjava-8-alpine AS profileInteractionServiceBuild
ARG STAGE=dev
WORKDIR /usr/src/profileInteractionService
COPY . .
RUN mvn package -P${STAGE} -DskipTests


FROM openjdk:8-jdk-alpine AS profileInteractionServiceRuntime
WORKDIR /app
COPY --from=profileInteractionServiceBuild /usr/src/profileInteractionService/target/profileInteractionService.jar ./
EXPOSE 8080
CMD java -jar profileInteractionService.jar