name := securebanking-openbanking-uk-rs
repo := europe-west4-docker.pkg.dev/sbat-gcr-develop/sapig-docker-artifact
tag  := $(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
helm_repo := forgerock-helm/secure-api-gateway/securebanking-openbanking-uk-rs/

clean:
	mvn clean

install:
	mvn -U install

docker: install
	mvn dockerfile:build dockerfile:push -DskipTests -DskipITs -Dtag=${tag} \
	  -DgcrRepo=${repo} --file secure-api-gateway-ob-uk-rs-server/pom.xml

package_helm:
ifndef version
	$(error A version must be supplied, Eg. make helm version=1.0.0)
endif
	helm dependency update _infra/helm/${name}
	helm template _infra/helm/${name}
	helm package _infra/helm/${name} --version ${version} --app-version ${version}
	#mv ./${name}-*.tgz ./${name}-${version}.tgz

publish_helm:
ifndef version
	$(error A version must be supplied, Eg. make helm version=1.0.0)
endif
	jf rt upload  ./*-${version}.tgz ${helm_repo}

dev: clean
	mvn install package -DskipTests -DskipITs -Dtag=latest -DgcrRepo=${repo} \
	  --file secure-api-gateway-ob-uk-rs-server/pom.xml

version:
	@echo $(tag)