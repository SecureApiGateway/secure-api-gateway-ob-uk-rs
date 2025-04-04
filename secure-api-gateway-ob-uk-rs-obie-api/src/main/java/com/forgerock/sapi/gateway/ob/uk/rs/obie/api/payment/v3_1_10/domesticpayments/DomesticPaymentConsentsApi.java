/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v3_1_10.domesticpayments;

import static com.forgerock.sapi.gateway.ob.uk.rs.obie.api.ApiConstants.HTTP_DATE_FORMAT;

import java.security.Principal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import uk.org.openbanking.datamodel.v3.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticConsent4;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticConsentResponse5;
import uk.org.openbanking.datamodel.v3.payment.OBWriteFundsConfirmationResponse1;

@jakarta.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2020-05-22T14:20:48.770Z")

@Api(value = "domestic-payment-consents", description = "the domestic-payment-consents API")
@RequestMapping(value = "/open-banking/v3.1.10/pisp")
public interface DomesticPaymentConsentsApi {

    @ApiOperation(value = "Create Domestic Payment Consents", nickname = "createDomesticPaymentConsents", notes = "", response = OBWriteDomesticConsentResponse5.class, authorizations = {
            @Authorization(value = "TPPOAuth2Security", scopes = {
                    @AuthorizationScope(scope = "payments", description = "Generic payment scope")
            })
    }, tags = {"Domestic Payments",})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Domestic Payment Consents Created", response = OBWriteDomesticConsentResponse5.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden", response = OBErrorResponse1.class),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 429, message = "Too Many Requests"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = OBErrorResponse1.class)})
    @RequestMapping(value = "/domestic-payment-consents",
            produces = {"application/json; charset=utf-8", "application/jose+jwe"},
            consumes = {"application/json; charset=utf-8", "application/jose+jwe"},
            method = RequestMethod.POST)
    ResponseEntity<OBWriteDomesticConsentResponse5> createDomesticPaymentConsents(
            @ApiParam(value = "Default", required = true)
            @Valid
            @RequestBody OBWriteDomesticConsent4 obWriteDomesticConsent4,

            @ApiParam(value = "An Authorisation Token as per https://tools.ietf.org/html/rfc6750", required = true)
            @RequestHeader(value = "Authorization", required = true) String authorization,

            @ApiParam(value = "Every request will be processed only once per x-idempotency-key.  The Idempotency Key will be valid for 24 hours. ", required = true)
            @RequestHeader(value = "x-idempotency-key", required = true) String xIdempotencyKey,

            @ApiParam(value = "A detached JWS signature of the body of the payload.", required = true)
            @RequestHeader(value = "x-jws-signature", required = true) String xJwsSignature,

            @ApiParam(value = "The time when the PSU last logged in with the TPP.  All dates in the HTTP headers are represented as RFC 7231 Full Dates. An example is below:  Sun, 10 Sep 2017 19:43:31 UTC")
            @RequestHeader(value = "x-fapi-auth-date", required = false)
            @DateTimeFormat(pattern = HTTP_DATE_FORMAT) String xFapiAuthDate,

            @ApiParam(value = "The PSU's IP address if the PSU is currently logged in with the TPP.")
            @RequestHeader(value = "x-fapi-customer-ip-address", required = false) String xFapiCustomerIpAddress,

            @ApiParam(value = "An RFC4122 UID used as a correlation id.")
            @RequestHeader(value = "x-fapi-interaction-id", required = false) String xFapiInteractionId,

            @ApiParam(value = "Indicates the user-agent that the PSU is using.")
            @RequestHeader(value = "x-customer-user-agent", required = false) String xCustomerUserAgent,

            @ApiParam(value = "OAuth2.0 client_id of the ApiClient making the request")
            @RequestHeader(value = "x-api-client-id") String apiClientId,

            HttpServletRequest request,

            Principal principal
    ) throws OBErrorResponseException;


    @ApiOperation(value = "Get Domestic Payment Consents", nickname = "getDomesticPaymentConsentsConsentId", notes = "", response = OBWriteDomesticConsentResponse5.class, authorizations = {
            @Authorization(value = "TPPOAuth2Security", scopes = {
                    @AuthorizationScope(scope = "payments", description = "Generic payment scope")
            })
    }, tags = {"Domestic Payments",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Domestic Payment Consents Read", response = OBWriteDomesticConsentResponse5.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden", response = OBErrorResponse1.class),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 429, message = "Too Many Requests"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = OBErrorResponse1.class)})
    @RequestMapping(value = "/domestic-payment-consents/{ConsentId}",
            produces = {"application/json; charset=utf-8", "application/jose+jwe"},
            method = RequestMethod.GET)
    ResponseEntity<OBWriteDomesticConsentResponse5> getDomesticPaymentConsentsConsentId(
            @ApiParam(value = "ConsentId", required = true)
            @PathVariable("ConsentId") String consentId,

            @ApiParam(value = "An Authorisation Token as per https://tools.ietf.org/html/rfc6750", required = true)
            @RequestHeader(value = "Authorization", required = true) String authorization,

            @ApiParam(value = "The time when the PSU last logged in with the TPP.  All dates in the HTTP headers are represented as RFC 7231 Full Dates. An example is below:  Sun, 10 Sep 2017 19:43:31 UTC")
            @RequestHeader(value = "x-fapi-auth-date", required = false)
            @DateTimeFormat(pattern = HTTP_DATE_FORMAT) String xFapiAuthDate,

            @ApiParam(value = "The PSU's IP address if the PSU is currently logged in with the TPP.")
            @RequestHeader(value = "x-fapi-customer-ip-address", required = false) String xFapiCustomerIpAddress,

            @ApiParam(value = "An RFC4122 UID used as a correlation id.")
            @RequestHeader(value = "x-fapi-interaction-id", required = false) String xFapiInteractionId,

            @ApiParam(value = "Indicates the user-agent that the PSU is using.")
            @RequestHeader(value = "x-customer-user-agent", required = false) String xCustomerUserAgent,

            @ApiParam(value = "OAuth2.0 client_id of the ApiClient making the request")
            @RequestHeader(value = "x-api-client-id") String apiClientId,

            HttpServletRequest request,

            Principal principal
    ) throws OBErrorResponseException;


    @ApiOperation(value = "Get Domestic Payment Consents",
            nickname = "getDomesticPaymentConsentsConsentIdFundsConfirmation",
            notes = "", response = OBWriteFundsConfirmationResponse1.class, authorizations = {
            @Authorization(value = "PSUOAuth2Security", scopes = {
                    @AuthorizationScope(scope = "payments", description = "Generic payment scope")
            })
    }, tags = {"Domestic Payments",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Domestic Payment Consents Read", response = OBWriteFundsConfirmationResponse1.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden", response = OBErrorResponse1.class),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 429, message = "Too Many Requests"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = OBErrorResponse1.class)})
    @RequestMapping(value = "/domestic-payment-consents/{ConsentId}/funds-confirmation",
            produces = {"application/json; charset=utf-8", "application/jose+jwe"},
            method = RequestMethod.GET)
    ResponseEntity<OBWriteFundsConfirmationResponse1> getDomesticPaymentConsentsConsentIdFundsConfirmation(
            @ApiParam(value = "ConsentId", required = true)
            @PathVariable("ConsentId") String consentId,

            @ApiParam(value = "An Authorisation Token as per https://tools.ietf.org/html/rfc6750", required = true)
            @RequestHeader(value = "Authorization", required = true) String authorization,

            @ApiParam(value = "The time when the PSU last logged in with the TPP.  All dates in the HTTP headers are represented as RFC 7231 Full Dates. An example is below:  Sun, 10 Sep 2017 19:43:31 UTC")
            @RequestHeader(value = "x-fapi-auth-date", required = false)
            @DateTimeFormat(pattern = HTTP_DATE_FORMAT) String xFapiAuthDate,

            @ApiParam(value = "The PSU's IP address if the PSU is currently logged in with the TPP.")
            @RequestHeader(value = "x-fapi-customer-ip-address", required = false) String xFapiCustomerIpAddress,

            @ApiParam(value = "An RFC4122 UID used as a correlation id.")
            @RequestHeader(value = "x-fapi-interaction-id", required = false) String xFapiInteractionId,

            @ApiParam(value = "Indicates the user-agent that the PSU is using.")
            @RequestHeader(value = "x-customer-user-agent", required = false) String xCustomerUserAgent,

            @ApiParam(value = "OAuth2.0 client_id of the ApiClient making the request")
            @RequestHeader(value = "x-api-client-id") String apiClientId,

            HttpServletRequest request,

            Principal principal
    ) throws OBErrorResponseException;

}