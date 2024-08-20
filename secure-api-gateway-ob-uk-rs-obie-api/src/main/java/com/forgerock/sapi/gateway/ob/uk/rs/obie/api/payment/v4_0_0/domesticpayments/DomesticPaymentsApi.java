/*
 * Copyright © 2020-2024 ForgeRock AS (obst@forgerock.com)
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
/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (7.2.0).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
package com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v4_0_0.domesticpayments;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import uk.org.openbanking.datamodel.v4.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomestic2;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticResponse5;
import uk.org.openbanking.datamodel.v4.payment.OBWritePaymentDetailsResponse1;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen")
@Validated
@Tag(name = "Domestic Payments", description = "the Domestic Payments API")
@RequestMapping(value = "/open-banking/v4.0.0/pisp")
public interface DomesticPaymentsApi {

    /**
     * POST /domestic-payments : Create Domestic Payments
     *
     * @param authorization An Authorisation Token as per https://tools.ietf.org/html/rfc6750 (required)
     * @param xIdempotencyKey Every request will be processed only once per x-idempotency-key.  The Idempotency Key will be valid for 24 hours.  (required)
     * @param xJwsSignature A detached JWS signature of the body of the payload. (required)
     * @param obWriteDomestic2 Default (required)
     * @param xFapiAuthDate The time when the PSU last logged in with the TPP.  All dates in the HTTP headers are represented as RFC 7231 Full Dates. An example is below:  Sun, 10 Sep 2017 19:43:31 UTC (optional)
     * @param xFapiCustomerIpAddress The PSU&#39;s IP address if the PSU is currently logged in with the TPP. (optional)
     * @param xFapiInteractionId An RFC4122 UID used as a correlation id. (optional)
     * @param xCustomerUserAgent Indicates the user-agent that the PSU is using. (optional)
     * @param apiClientId OAuth2.0 client_id of the ApiClient making the request
     * @return Domestic Payments Created (status code 201)
     *         or Bad request (status code 400)
     *         or Unauthorized (status code 401)
     *         or Forbidden (status code 403)
     *         or Method Not Allowed (status code 405)
     *         or Not Acceptable (status code 406)
     *         or Conflict (status code 409)
     *         or Unsupported Media Type (status code 415)
     *         or Too Many Requests (status code 429)
     *         or Internal Server Error (status code 500)
     */
    @Operation(
        operationId = "createDomesticPayments",
        summary = "Create Domestic Payments",
        tags = { "Domestic Payments" },
        responses = {
            @ApiResponse(responseCode = "201", description = "Domestic Payments Created", content = {
                @Content(mediaType = "application/json; charset=utf-8", schema = @Schema(implementation = OBWriteDomesticResponse5.class)),
                @Content(mediaType = "application/json", schema = @Schema(implementation = OBWriteDomesticResponse5.class)),
                @Content(mediaType = "application/jose+jwe", schema = @Schema(implementation = OBWriteDomesticResponse5.class))
            }),
            @ApiResponse(responseCode = "400", description = "Bad request", content = {
                @Content(mediaType = "application/json; charset=utf-8", schema = @Schema(implementation = OBErrorResponse1.class)),
                @Content(mediaType = "application/json", schema = @Schema(implementation = OBErrorResponse1.class)),
                @Content(mediaType = "application/jose+jwe", schema = @Schema(implementation = OBErrorResponse1.class))
            }),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = {
                @Content(mediaType = "application/json; charset=utf-8", schema = @Schema(implementation = OBErrorResponse1.class)),
                @Content(mediaType = "application/json", schema = @Schema(implementation = OBErrorResponse1.class)),
                @Content(mediaType = "application/jose+jwe", schema = @Schema(implementation = OBErrorResponse1.class))
            }),
            @ApiResponse(responseCode = "405", description = "Method Not Allowed"),
            @ApiResponse(responseCode = "406", description = "Not Acceptable"),
            @ApiResponse(responseCode = "409", description = "Conflict", content = {
                @Content(mediaType = "application/json; charset=utf-8", schema = @Schema(implementation = OBErrorResponse1.class)),
                @Content(mediaType = "application/json", schema = @Schema(implementation = OBErrorResponse1.class)),
                @Content(mediaType = "application/jose+jwe", schema = @Schema(implementation = OBErrorResponse1.class))
            }),
            @ApiResponse(responseCode = "415", description = "Unsupported Media Type"),
            @ApiResponse(responseCode = "429", description = "Too Many Requests"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
                @Content(mediaType = "application/json; charset=utf-8", schema = @Schema(implementation = OBErrorResponse1.class)),
                @Content(mediaType = "application/json", schema = @Schema(implementation = OBErrorResponse1.class)),
                @Content(mediaType = "application/jose+jwe", schema = @Schema(implementation = OBErrorResponse1.class))
            })
        },
        security = {
            @SecurityRequirement(name = "PSUOAuth2Security", scopes={ "payments" })
        }
    )
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/domestic-payments",
        produces = { "application/json; charset=utf-8", "application/json", "application/jose+jwe" },
        consumes = { "application/json; charset=utf-8", "application/json", "application/jose+jwe" }
    )
    
    ResponseEntity<OBWriteDomesticResponse5> createDomesticPayments(
        @NotNull @Parameter(name = "Authorization", description = "An Authorisation Token as per https://tools.ietf.org/html/rfc6750", required = true, in = ParameterIn.HEADER) @RequestHeader(value = "Authorization", required = true) String authorization,
        @NotNull @Pattern(regexp = "^(?!\\s)(.*)(\\S)$") @Size(max = 40) @Parameter(name = "x-idempotency-key", description = "Every request will be processed only once per x-idempotency-key.  The Idempotency Key will be valid for 24 hours. ", required = true, in = ParameterIn.HEADER) @RequestHeader(value = "x-idempotency-key", required = true) String xIdempotencyKey,
        @NotNull @Parameter(name = "x-jws-signature", description = "A detached JWS signature of the body of the payload.", required = true, in = ParameterIn.HEADER) @RequestHeader(value = "x-jws-signature", required = true) String xJwsSignature,
        @Parameter(name = "OBWriteDomestic2", description = "Default", required = true) @Valid @RequestBody OBWriteDomestic2 obWriteDomestic2,
        @Pattern(regexp = "^(Mon|Tue|Wed|Thu|Fri|Sat|Sun), \\d{2} (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) \\d{4} \\d{2}:\\d{2}:\\d{2} (GMT|UTC)$") @Parameter(name = "x-fapi-auth-date", description = "The time when the PSU last logged in with the TPP.  All dates in the HTTP headers are represented as RFC 7231 Full Dates. An example is below:  Sun, 10 Sep 2017 19:43:31 UTC", in = ParameterIn.HEADER) @RequestHeader(value = "x-fapi-auth-date", required = false) String xFapiAuthDate,
        @Parameter(name = "x-fapi-customer-ip-address", description = "The PSU's IP address if the PSU is currently logged in with the TPP.", in = ParameterIn.HEADER) @RequestHeader(value = "x-fapi-customer-ip-address", required = false) String xFapiCustomerIpAddress,
        @Parameter(name = "x-fapi-interaction-id", description = "An RFC4122 UID used as a correlation id.", in = ParameterIn.HEADER) @RequestHeader(value = "x-fapi-interaction-id", required = false) String xFapiInteractionId,
        @Parameter(name = "x-customer-user-agent", description = "Indicates the user-agent that the PSU is using.", in = ParameterIn.HEADER) @RequestHeader(value = "x-customer-user-agent", required = false) String xCustomerUserAgent,
        @Parameter(name = "x-api-client-id", description = "OAuth2.0 client_id of the ApiClient making the request", in = ParameterIn.HEADER) @RequestHeader(value = "x-api-client-id") String apiClientId
    );


    /**
     * GET /domestic-payments/{DomesticPaymentId} : Get Domestic Payments
     *
     * @param domesticPaymentId DomesticPaymentId (required)
     * @param authorization An Authorisation Token as per https://tools.ietf.org/html/rfc6750 (required)
     * @param xFapiAuthDate The time when the PSU last logged in with the TPP.  All dates in the HTTP headers are represented as RFC 7231 Full Dates. An example is below:  Sun, 10 Sep 2017 19:43:31 UTC (optional)
     * @param xFapiCustomerIpAddress The PSU&#39;s IP address if the PSU is currently logged in with the TPP. (optional)
     * @param xFapiInteractionId An RFC4122 UID used as a correlation id. (optional)
     * @param xCustomerUserAgent Indicates the user-agent that the PSU is using. (optional)
     * @param apiClientId OAuth2.0 client_id of the ApiClient making the request
     * @return Domestic Payments Read (status code 200)
     *         or Bad request (status code 400)
     *         or Unauthorized (status code 401)
     *         or Forbidden (status code 403)
     *         or Method Not Allowed (status code 405)
     *         or Not Acceptable (status code 406)
     *         or Too Many Requests (status code 429)
     *         or Internal Server Error (status code 500)
     */
    @Operation(
        operationId = "getDomesticPaymentsDomesticPaymentId",
        summary = "Get Domestic Payments",
        tags = { "Domestic Payments" },
        responses = {
            @ApiResponse(responseCode = "200", description = "Domestic Payments Read", content = {
                @Content(mediaType = "application/json; charset=utf-8", schema = @Schema(implementation = OBWriteDomesticResponse5.class)),
                @Content(mediaType = "application/json", schema = @Schema(implementation = OBWriteDomesticResponse5.class)),
                @Content(mediaType = "application/jose+jwe", schema = @Schema(implementation = OBWriteDomesticResponse5.class))
            }),
            @ApiResponse(responseCode = "400", description = "Bad request", content = {
                @Content(mediaType = "application/json; charset=utf-8", schema = @Schema(implementation = OBErrorResponse1.class)),
                @Content(mediaType = "application/json", schema = @Schema(implementation = OBErrorResponse1.class)),
                @Content(mediaType = "application/jose+jwe", schema = @Schema(implementation = OBErrorResponse1.class))
            }),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = {
                @Content(mediaType = "application/json; charset=utf-8", schema = @Schema(implementation = OBErrorResponse1.class)),
                @Content(mediaType = "application/json", schema = @Schema(implementation = OBErrorResponse1.class)),
                @Content(mediaType = "application/jose+jwe", schema = @Schema(implementation = OBErrorResponse1.class))
            }),
            @ApiResponse(responseCode = "405", description = "Method Not Allowed"),
            @ApiResponse(responseCode = "406", description = "Not Acceptable"),
            @ApiResponse(responseCode = "429", description = "Too Many Requests"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
                @Content(mediaType = "application/json; charset=utf-8", schema = @Schema(implementation = OBErrorResponse1.class)),
                @Content(mediaType = "application/json", schema = @Schema(implementation = OBErrorResponse1.class)),
                @Content(mediaType = "application/jose+jwe", schema = @Schema(implementation = OBErrorResponse1.class))
            })
        },
        security = {
            @SecurityRequirement(name = "TPPOAuth2Security", scopes={ "payments" })
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/domestic-payments/{DomesticPaymentId}",
        produces = { "application/json; charset=utf-8", "application/json", "application/jose+jwe" }
    )
    
    ResponseEntity<OBWriteDomesticResponse5> getDomesticPaymentsDomesticPaymentId(
        @Parameter(name = "DomesticPaymentId", description = "DomesticPaymentId", required = true, in = ParameterIn.PATH) @PathVariable("DomesticPaymentId") String domesticPaymentId,
        @NotNull @Parameter(name = "Authorization", description = "An Authorisation Token as per https://tools.ietf.org/html/rfc6750", required = true, in = ParameterIn.HEADER) @RequestHeader(value = "Authorization", required = true) String authorization,
        @Pattern(regexp = "^(Mon|Tue|Wed|Thu|Fri|Sat|Sun), \\d{2} (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) \\d{4} \\d{2}:\\d{2}:\\d{2} (GMT|UTC)$") @Parameter(name = "x-fapi-auth-date", description = "The time when the PSU last logged in with the TPP.  All dates in the HTTP headers are represented as RFC 7231 Full Dates. An example is below:  Sun, 10 Sep 2017 19:43:31 UTC", in = ParameterIn.HEADER) @RequestHeader(value = "x-fapi-auth-date", required = false) String xFapiAuthDate,
        @Parameter(name = "x-fapi-customer-ip-address", description = "The PSU's IP address if the PSU is currently logged in with the TPP.", in = ParameterIn.HEADER) @RequestHeader(value = "x-fapi-customer-ip-address", required = false) String xFapiCustomerIpAddress,
        @Parameter(name = "x-fapi-interaction-id", description = "An RFC4122 UID used as a correlation id.", in = ParameterIn.HEADER) @RequestHeader(value = "x-fapi-interaction-id", required = false) String xFapiInteractionId,
        @Parameter(name = "x-customer-user-agent", description = "Indicates the user-agent that the PSU is using.", in = ParameterIn.HEADER) @RequestHeader(value = "x-customer-user-agent", required = false) String xCustomerUserAgent,
        @Parameter(name = "x-api-client-id", description = "OAuth2.0 client_id of the ApiClient making the request", in = ParameterIn.HEADER) @RequestHeader(value = "x-api-client-id") String apiClientId
    );


    /**
     * GET /domestic-payments/{DomesticPaymentId}/payment-details : Get Payment Details
     *
     * @param domesticPaymentId DomesticPaymentId (required)
     * @param authorization An Authorisation Token as per https://tools.ietf.org/html/rfc6750 (required)
     * @param xFapiAuthDate The time when the PSU last logged in with the TPP.  All dates in the HTTP headers are represented as RFC 7231 Full Dates. An example is below:  Sun, 10 Sep 2017 19:43:31 UTC (optional)
     * @param xFapiCustomerIpAddress The PSU&#39;s IP address if the PSU is currently logged in with the TPP. (optional)
     * @param xFapiInteractionId An RFC4122 UID used as a correlation id. (optional)
     * @param xCustomerUserAgent Indicates the user-agent that the PSU is using. (optional)
     * @param apiClientId OAuth2.0 client_id of the ApiClient making the request
     * @return Payment Details Read (status code 200)
     *         or Bad request (status code 400)
     *         or Unauthorized (status code 401)
     *         or Forbidden (status code 403)
     *         or Not found (status code 404)
     *         or Method Not Allowed (status code 405)
     *         or Not Acceptable (status code 406)
     *         or Too Many Requests (status code 429)
     *         or Internal Server Error (status code 500)
     */
    @Operation(
        operationId = "getDomesticPaymentsDomesticPaymentIdPaymentDetails",
        summary = "Get Payment Details",
        tags = { "Payment Details" },
        responses = {
            @ApiResponse(responseCode = "200", description = "Payment Details Read", content = {
                @Content(mediaType = "application/json; charset=utf-8", schema = @Schema(implementation = OBWritePaymentDetailsResponse1.class)),
                @Content(mediaType = "application/json", schema = @Schema(implementation = OBWritePaymentDetailsResponse1.class)),
                @Content(mediaType = "application/jose+jwe", schema = @Schema(implementation = OBWritePaymentDetailsResponse1.class))
            }),
            @ApiResponse(responseCode = "400", description = "Bad request", content = {
                @Content(mediaType = "application/json; charset=utf-8", schema = @Schema(implementation = OBErrorResponse1.class)),
                @Content(mediaType = "application/json", schema = @Schema(implementation = OBErrorResponse1.class)),
                @Content(mediaType = "application/jose+jwe", schema = @Schema(implementation = OBErrorResponse1.class))
            }),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = {
                @Content(mediaType = "application/json; charset=utf-8", schema = @Schema(implementation = OBErrorResponse1.class)),
                @Content(mediaType = "application/json", schema = @Schema(implementation = OBErrorResponse1.class)),
                @Content(mediaType = "application/jose+jwe", schema = @Schema(implementation = OBErrorResponse1.class))
            }),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "405", description = "Method Not Allowed"),
            @ApiResponse(responseCode = "406", description = "Not Acceptable"),
            @ApiResponse(responseCode = "429", description = "Too Many Requests"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
                @Content(mediaType = "application/json; charset=utf-8", schema = @Schema(implementation = OBErrorResponse1.class)),
                @Content(mediaType = "application/json", schema = @Schema(implementation = OBErrorResponse1.class)),
                @Content(mediaType = "application/jose+jwe", schema = @Schema(implementation = OBErrorResponse1.class))
            })
        },
        security = {
            @SecurityRequirement(name = "TPPOAuth2Security", scopes={ "payments" })
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/domestic-payments/{DomesticPaymentId}/payment-details",
        produces = { "application/json; charset=utf-8", "application/json", "application/jose+jwe" }
    )
    
    ResponseEntity<OBWritePaymentDetailsResponse1> getDomesticPaymentsDomesticPaymentIdPaymentDetails(
        @Parameter(name = "DomesticPaymentId", description = "DomesticPaymentId", required = true, in = ParameterIn.PATH) @PathVariable("DomesticPaymentId") String domesticPaymentId,
        @NotNull @Parameter(name = "Authorization", description = "An Authorisation Token as per https://tools.ietf.org/html/rfc6750", required = true, in = ParameterIn.HEADER) @RequestHeader(value = "Authorization", required = true) String authorization,
        @Pattern(regexp = "^(Mon|Tue|Wed|Thu|Fri|Sat|Sun), \\d{2} (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) \\d{4} \\d{2}:\\d{2}:\\d{2} (GMT|UTC)$") @Parameter(name = "x-fapi-auth-date", description = "The time when the PSU last logged in with the TPP.  All dates in the HTTP headers are represented as RFC 7231 Full Dates. An example is below:  Sun, 10 Sep 2017 19:43:31 UTC", in = ParameterIn.HEADER) @RequestHeader(value = "x-fapi-auth-date", required = false) String xFapiAuthDate,
        @Parameter(name = "x-fapi-customer-ip-address", description = "The PSU's IP address if the PSU is currently logged in with the TPP.", in = ParameterIn.HEADER) @RequestHeader(value = "x-fapi-customer-ip-address", required = false) String xFapiCustomerIpAddress,
        @Parameter(name = "x-fapi-interaction-id", description = "An RFC4122 UID used as a correlation id.", in = ParameterIn.HEADER) @RequestHeader(value = "x-fapi-interaction-id", required = false) String xFapiInteractionId,
        @Parameter(name = "x-customer-user-agent", description = "Indicates the user-agent that the PSU is using.", in = ParameterIn.HEADER) @RequestHeader(value = "x-customer-user-agent", required = false) String xCustomerUserAgent,
        @Parameter(name = "x-api-client-id", description = "OAuth2.0 client_id of the ApiClient making the request", in = ParameterIn.HEADER) @RequestHeader(value = "x-api-client-id") String apiClientId
    );

}
