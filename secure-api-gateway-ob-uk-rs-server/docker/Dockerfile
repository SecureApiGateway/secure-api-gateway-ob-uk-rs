FROM gcr.io/forgerock-io/java-17:latest

ARG JAR_FILE

# dependencies for wait-for.sh
RUN apt-get -q update && apt-get -qy install netcat-traditional

RUN mkdir /app
RUN groupadd -r rcs && useradd -r -s /bin/false -g rcs rcs

WORKDIR /app
COPY $JAR_FILE /app/securebanking-openbanking-uk-rs.jar

# wait-for.sh for checking if other services are ready before starting spring
COPY docker/scripts/*.sh /app/

RUN chown -R rcs:rcs /app
USER rcs

CMD ["java", "-jar", "securebanking-openbanking-uk-rs.jar"]
