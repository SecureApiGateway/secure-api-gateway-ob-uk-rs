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
package com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v4_0_0.scheduledpayments;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import uk.org.openbanking.datamodel.v4.account.OBReadScheduledPayment3;
import uk.org.openbanking.datamodel.v4.error.OBErrorResponse1;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen")
@Validated
@Tag(name = "Scheduled Payments", description = "the Scheduled Payments API")
@RequestMapping(value = "/open-banking/v4.0.0/aisp")
public interface ScheduledPaymentsApi {

    /**
     * GET /scheduled-payments : Get Scheduled Payments
     *
     * @param authorization          An Authorisation Token as per https://tools.ietf.org/html/rfc6750 (required)
     * @param xFapiAuthDate          The time when the PSU last logged in with the TPP.  All dates in the HTTP headers are represented as RFC 7231 Full Dates. An example is below:  Sun, 10 Sep 2017 19:43:31 UTC (optional)
     * @param xFapiCustomerIpAddress The PSU&#39;s IP address if the PSU is currently logged in with the TPP. (optional)
     * @param xFapiInteractionId     An RFC4122 UID used as a correlation id. (optional)
     * @param xCustomerUserAgent     Indicates the user-agent that the PSU is using. (optional)
     * @return Scheduled Payments Read (status code 200)
     * or Bad request (status code 400)
     * or Unauthorized (status code 401)
     * or Forbidden (status code 403)
     * or Not found (status code 404)
     * or Method Not Allowed (status code 405)
     * or Not Acceptable (status code 406)
     * or Too Many Requests (status code 429)
     * or Internal Server Error (status code 500)
     */
    @Operation(
            operationId = "getScheduledPayments",
            summary = "Get Scheduled Payments",
            tags = {"Scheduled Payments"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Scheduled Payments Read", content = {
                            @Content(mediaType = "application/json; charset=utf-8", schema = @Schema(implementation = OBReadScheduledPayment3.class)),
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OBReadScheduledPayment3.class)),
                            @Content(mediaType = "application/jose+jwe", schema = @Schema(implementation = OBReadScheduledPayment3.class))
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
                    @SecurityRequirement(name = "PSUOAuth2Security", scopes = {"accounts"})
            }
    )
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/scheduled-payments",
            produces = {"application/json; charset=utf-8", "application/json", "application/jose+jwe"}
    )
    ResponseEntity<OBReadScheduledPayment3> getScheduledPayments(
            @NotNull @Parameter(name = "Authorization", description = "An Authorisation Token as per https://tools.ietf.org/html/rfc6750", required = true, in = ParameterIn.HEADER) @RequestHeader(value = "Authorization", required = true) String authorization,
            @Pattern(regexp = "^(Mon|Tue|Wed|Thu|Fri|Sat|Sun), \\d{2} (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) \\d{4} \\d{2}:\\d{2}:\\d{2} (GMT|UTC)$") @Parameter(name = "x-fapi-auth-date", description = "The time when the PSU last logged in with the TPP.  All dates in the HTTP headers are represented as RFC 7231 Full Dates. An example is below:  Sun, 10 Sep 2017 19:43:31 UTC", in = ParameterIn.HEADER) @RequestHeader(value = "x-fapi-auth-date", required = false) String xFapiAuthDate,
            @Parameter(name = "x-fapi-customer-ip-address", description = "The PSU's IP address if the PSU is currently logged in with the TPP.", in = ParameterIn.HEADER) @RequestHeader(value = "x-fapi-customer-ip-address", required = false) String xFapiCustomerIpAddress,
            @Parameter(name = "x-fapi-interaction-id", description = "An RFC4122 UID used as a correlation id.", in = ParameterIn.HEADER) @RequestHeader(value = "x-fapi-interaction-id", required = false) String xFapiInteractionId,
            @Parameter(name = "x-customer-user-agent", description = "Indicates the user-agent that the PSU is using.", in = ParameterIn.HEADER) @RequestHeader(value = "x-customer-user-agent", required = false) String xCustomerUserAgent
    );

}
