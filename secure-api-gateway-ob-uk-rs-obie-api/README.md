## secure-api-gateway-ob-uk-rs-obie-api

This module contains the interface definitions which closely match the OBIE Read/Write API for a UK Open Banking Resource Server.

## Api Class generation
Many of the classes are generated from the OB Swagger documentation. The project is setup to make it easy to generate
the  OB model classes and skeleton API classes using Maven. For efficiency, the default maven profile does not generate
the code, but it is easy to do so using `code-gen` profile (see below).

The configuration for the swagger generation is currently within `secure-api-gateway-ob-uk-rs-obie-api/pom.xml`
and the swagger specification is within `secure-api-gateway-ob-uk-rs-obie-api/src/main/resources/specification`.

When a new version of OB API is released, the following steps should be performed:
1. Download the Swagger yaml files from OB Spec pages (https://github.com/OpenBankingUK/read-write-api-specs/releases).
   As of v3.1.8, there are swagger files for Accounts, Payments, Funds Confirmation, Events and Variable Recurring Payments.
1. Place the swagger files under `secure-api-gateway-ob-uk-rs-obie-api/src/main/resources/specification` (replacing existing ones where applicable).
1. Override the configuration on `secure-api-gateway-ob-uk-rs-obie-api/pom.xml` to generate the proper source code.
1. Run ```mvn clean install -Pcode-gen```
   > This will generate classes into `secure-api-gateway-ob-uk-rs-obie-api/target/generated-sources/swagger`
1. Check the generated files:
    1. There is two modules where will live the generated classes:
       - Interfaces: `secure-api-gateway-ob-uk-rs-obie-api/src/main/java/[...]/api/obie`
       - Implementations: `secure-api-gateway-ob-uk-rs-server/src/main/java/[...]/api/obie`
       to identify those classes generated that can be deleted to use a shared class and refactor the generated classes to use the shared ones.
    2. > Review the generated classes to identify those classes that could be moved as a shared class.
1. Copy them into the appropriate source folders defined above: (e.g. `secure-api-gateway-ob-uk-rs-obie-api/src/main/java/[...]/api/obie`).
1. Uncomment the relevant `<inputSpec>` listing within the `openapi-generator-maven-plugin` in the pom for the next
   swagger spec (and repeat for each new swagger YAML file).
1. If using Intellij, run format and optimise imports on newly generated files.
1. Run build to ensure everything compiles and copyrights are generated for new source files.
1. Commit and raise PR.
