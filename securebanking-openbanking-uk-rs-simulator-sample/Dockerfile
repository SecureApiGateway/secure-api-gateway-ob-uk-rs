FROM adoptopenjdk/openjdk14:jre-14.0.2_12

ARG JAR_FILE

RUN mkdir /app
RUN groupadd -r rcs && useradd -r -s /bin/false -g rcs rcs

WORKDIR /app
COPY $JAR_FILE /app/securebanking-openbanking-uk-rs.jar
RUN chown -R rcs:rcs /app
USER rcs

CMD ["java", "-jar", "securebanking-openbanking-uk-rs.jar"]
