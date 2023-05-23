# Validation Framework
This module provides a framework for validating objects and reporting any errors.

## Core Module

`secure-api-gateway-ob-uk-rs-validation-core` module provides the core interfaces and classes which make up the
framework. These aim to be generic and can be applied to different use cases.

### Key classes
#### [com.forgerock.sapi.gateway.ob.uk.rs.validation.Validator<T,E>](secure-api-gateway-ob-uk-rs-validation-core/src/main/java/com/forgerock/sapi/gateway/ob/uk/rs/validation/Validator.java)
This defines the behaviour of a validator

- Type param `T` is the type of object that the validator can be applied to
- Type param `E` is the type of error object that the validator reports

The interface has a single method:
```java
ValidationResult<E> validate(T obj);
```

Validators MUST NOT throw any exceptions, all errors must be communicated by the ValidationResult. It is the responsibility
of the caller to inspect the result and take appropriate action, for errors this may mean throwing an exception.

This mechanism allows implementors the option of doing full validation and collecting all the errors, or failing-fast returning the first error encountered.

#### [com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult<E>](secure-api-gateway-ob-uk-rs-validation-core/src/main/java/com/forgerock/sapi/gateway/ob/uk/rs/validation/ValidationResult.java) 
This defines the result of a validation.

- Type param `E` is the type of error object that this result may contain

The result can either be valid (successful) or invalid (contains 1 or more error objects)


## OBIE Module
`secure-api-gateway-ob-uk-rs-validation-obie` module provides OBIE (Open Banking UK) specific validation classes.

Validation errors are communicated using the OBIE data-model class: `uk.org.openbanking.datamodel.error.OBError1`
This can then be transformed into a HTTP response which conforms to the OBIE spec.

### Key classes
#### [com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.BaseOBValidator<T>](secure-api-gateway-ob-uk-rs-validation-obie/src/main/java/com/forgerock/sapi/gateway/ob/uk/rs/validation/obie/BaseOBValidator.java) 
Abstract base class which implements the Validator interface and provides some common functionality for all Open Banking specific validators.

#### [com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidationService<T>](secure-api-gateway-ob-uk-rs-validation-obie/src/main/java/com/forgerock/sapi/gateway/ob/uk/rs/validation/obie/OBValidationService.java)
Service class which applies one or more validators to an object, then throws any error results as OBErrorResponseException.

The OBErrorResponseException is the mechanism that the rs-server uses to construct OBIE error responses to send in the HTTP Response.

#### Specific Validator Implementations
A validator needs to be created for each OB API type that we wish to validate, validators can choose to be composed of other validators in order
to validate particular fields or can implement all validation rules for a particular object (and its nested types) themselves.

The naming convention for validators is "${TypeToValidate}Validator".

Example validator impl: [com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.account.consent.OBReadConsent1Validator](secure-api-gateway-ob-uk-rs-validation-obie/src/main/java/com/forgerock/sapi/gateway/ob/uk/rs/validation/obie/account/consent/OBReadConsent1Validator.java),
this is responsible for validating OBReadConsent1 objects (Account Access Consents).