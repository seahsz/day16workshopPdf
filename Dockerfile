FROM eclipse-temurin:23.0.1_11-jdk AS builder

WORKDIR /compiledDir

COPY src src
COPY .mvn .mvn
COPY pom.xml .
COPY mvnw .

RUN chmod a+x ./mvnw && ./mvnw package -Dmaven.test.skip=true

# Doing MultiStage, do not need ENTRYPOINT

FROM eclipse-temurin:23.0.1_11-jdk

WORKDIR /app

COPY --from=builder /compiledDir/target/day16workshopPdf-0.0.1-SNAPSHOT.jar day16workshopPdf.jar

# ENV SERVER_PORT=3000 (for local use) --> for Railway need to user PORT=...
ENV SPRING_DATA_REDIS_HOST=localhost
ENV SPRING_DATA_REDIS_PORT=6379
ENV SPRING_DATA_REDIS_USERNAME=
ENV SPRING_DATA_REDIS_PASSWORD=

ENV PORT=4500

EXPOSE ${PORT}
# EXPOSE ${SERVER_PORT}


ENTRYPOINT java -jar day16workshopPdf.jar