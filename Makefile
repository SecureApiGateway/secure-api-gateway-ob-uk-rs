name := securebanking-openbanking-uk-rs
repo := sbat-gcr-develop
tag  := $(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

.PHONY: all
all: clean test package

clean:
	rm -f ${name}.jar
	mvn clean

verify: clean
	mvn verify

docker: clean
	mvn package dockerfile:push -DskipTests=true -Dtag=${tag} \
	  -DgcrRepo=${repo} --file securebanking-openbanking-uk-rs-simulator-sample/pom.xml

helm:
ifndef version
	$(error A version must be supplied, Eg. make helm version=1.0.0)
endif
	helm dep up _infra/helm/${name}
	helm template _infra/helm/${name}
	helm package _infra/helm/${name} --version ${version}

dev: clean
	mvn package -DskipTests=true -Dtag=latest -DgcrRepo=${repo} \
	  --file securebanking-openbanking-uk-rs-simulator-sample/pom.xml

version:
	@echo $(tag)
