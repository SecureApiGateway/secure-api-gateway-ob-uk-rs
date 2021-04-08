name := securebanking-openbanking-uk-rs
repo := sbat-gcr-develop

.PHONY: all
all: clean test package

clean:
	rm -f ${name}.jar
	mvn clean

test:
	mvn verify

package:
	mvn package -DskipTests -DskipITs -DdockerCompose.skip -Ddockerfile.skip --file pom.xml

docker: clean package
ifndef tag
	$(error "You must supply a docker tag")
endif
	cp ${name}-simulator-sample/target/${name}-*.jar ./${name}.jar
	docker build -t eu.gcr.io/${repo}/securebanking/${name}:${tag} .
	docker push eu.gcr.io/${repo}/securebanking/${name}:${tag}