FROM adoptopenjdk/openjdk14:jre-14.0.2_12

RUN mkdir /app
RUN groupadd -r rs && useradd -r -s /bin/false -g rs rs

WORKDIR /app
COPY securebanking-openbanking-uk-rs.jar /app
RUN chown -R rs:rs /app
USER rs

CMD ["java", "-jar", "securebanking-openbanking-uk-rs.jar"]