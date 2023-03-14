## Secure API Gateway UK Open Banking RS

A multi-module maven project providing a UK Open Banking Resource Server, including a bank simulator, for the Secure API Gateway.

### Setting up Maven

Download and install Maven settings.xml file by running the command below and substituting in your backstage username and password.

```bash
curl -u $BACKSTAGE_USERNAME http://maven.forgerock.org/repo/private-releases/settings.xml > ~/.m2/settings.xml
```

### Build the project

#### Maven build

From the command line, simply run:

```bash
mvn clean install
```
### Spring config
This module is built using Spring Boot and makes use of Spring Properties driven configuration.

`secure-api-gateway-ob-uk-rs-server` module contains the Spring configuration, see: [secure-api-gateway-ob-uk-rs-server/src/main/resources/application.yml](secure-api-gateway-ob-uk-rs-server/src/main/resources/application.yml).

This will run any JUnit/Spring integration tests and build the required JAR file and docker image.

#### Deployment Specific Config
The Spring config contains sensible defaults for configuration properties, when there is no sensible default then the property is left undefined and the application will fail with an exception at startup.

In this section we will discuss the config that is deployment specific, this config needs to be provided in order to run the application.


| Property                 | Description                                                                                                                                                                                                                                                                                                                                                                                                                            |
|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| consent.repo.uri         | Base URI of the Consent Repo API                                                                                                                                                                                                                                                                                                                                                                                                       |
| rs.discovery.financialId | OB Organisation Id of the ASPSP running the simulator                                                                                                                                                                                                                                                                                                                                                                                  |                                                                                                                                                                                                                                                                                                                                                                                 |
| spring.data.mongodb.*    | <p>RS uses mongo as the datastore, configure the Spring Data MongoDB properties required to connect to your instance</p><p>See https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#appendix.application-properties.data documentation, there are lots of options available with the spring.data.mongodb prefix. By default Spring Data will attempt to connect to a local mongodb database.</p> | 



The recommended way to supply these values is by using OS Environment Variables.

### How to run

**Run docker compose**
> Config server profile properties location from local volume (`securebanking-openbanking-uk-rcs-sample/docker/config`)
```shell
./securebanking-openbanking-uk-rs-simulator-sample/docker/run-docker-compose-local.sh
```

### Supported APIs
Upon starting the application, a list of supported APIs can be obtained dynamically from two different endpoints:

1. Discovery Endpoint
1. Swagger Specification Endpoint
 
#### Discovery Endpoint
The application has a "Discovery Endpoint" which lists all the supported URLs. This is the Open Banking Read/Write API
that the application has implemented. The Discovery Endpoint can be viewed in a browser by visiting:

```http://<host>:<port>/open-banking/discovery```

> Substitute `<host>` and `<port` as necessary

#### Swagger Specification Endpoint
The application's swagger documentation can be obtained from the following URL:

```shell
http://<host>:<port>/api-docs
``` 
```shell
http://<host>:<port>/swagger-resources
http://<host>:<port>/swagger-ui/ # important the slash at the end
```
> Substitute `<host>` and `<port` as necessary

This provides the Open Banking Read/Write Apis that the application is able
to support (regardless of whether they have been disabled in the configuration). Importantly, this reveals any
additional headers or request parameters that are required by the simulator.

> Any additional headers/parameters will be provided automatically to a bank's RS, if it is used instead
>of ForgeRock's RS simulator).

#### Enable/Disable API Endpoints
By default, all implemented API endpoints are enabled, however it is possible to explicitly disable them in the
application's config.

```
rs:
  discovery:
    financialId: 0015800001041REAAY
    versions:
      # v3.0 to v3.1.4 are enabled by default but set to `true` here for completeness
      v3.0: true
      v3.1: true
      v3.1.1: true
      v3.1.2: true
      v3.1.3: true
      v3.1.4: true

      # Disable all v3.1.5 and v3.1.6 endpoints
      v3.1.5: false
      v3.1.6: false

    apis:
      # Disable Create/Get DomesticPaymentConsent across all versions
        CreateDomesticPaymentConsent: false      
        GetDomesticPaymentConsent: false

    versionApiOverrides:
      # Disable Get statement endpoints in v3.1.3 and v3.1.4
      v3_1_3:
        GetStatements: false
        GetAccountStatements: false
      v3_1_4:
        GetStatements: false
        GetAccountStatements: false
```

As can be deduced from this config, Read/Write API endpoints can be disabled (resulting in a 404 error response) by one
of three ways:

1. **versions**: By specifying a complete version of the API to block. In the above example, any requests to v3.1.4
or v3.1.5 will be blocked.
1. **apis**: By specifying the name of the endpoint (which can be derived from the Discovery endpoint). In the above
example, `CreateDomesticPaymentConsent` and `GetDomesticPaymentConsent` are disabled in all versions.
1. **versionApiOverrides**: Or more specifically, by listing both the version and name of the endpoint. In the above
example, `GetStatements` and `GetAccountStatements` will be blocked in v3.1.3 and v3.1.4, but will work in the versions
prior to this.
