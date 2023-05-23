/*
 * Copyright © 2020-2022 ForgeRock AS (obst@forgerock.com)
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
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (5.2.1).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
package com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v3_1_10.vrp;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
import uk.org.openbanking.datamodel.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.vrp.OBVRPFundsConfirmationRequest;
import uk.org.openbanking.datamodel.vrp.OBVRPFundsConfirmationResponse;

/**
 * NOTE: API operations other than funds-confirmation have been removed from this generated code.
 * The other Consent API operations are implemented in the gateway component at the time of writing.
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen")
@Validated
@Api(value = "domestic-vrp-consents", description = "the domestic-vrp-consents API")
@RequestMapping(value = "/open-banking/v3.1.10/pisp")
public interface DomesticVrpConsentsApi {

    /**
     * POST /domestic-vrp-consents/{ConsentId}/funds-confirmation : Confirm availability of funds for a VRP
     * Confirm availability of funds for a VRP
     *
     * @param consentId ConsentId (required)
     * @param authorization An Authorisation Token as per https://tools.ietf.org/html/rfc6750 (required)
     * @param xJwsSignature A detached JWS signature of the body of the payload. (required)
     * @param obVRPFundsConfirmationRequest Default (required)
     * @param xFapiAuthDate The time when the PSU last logged in with the TPP.  All dates in the HTTP headers are represented as RFC 7231 Full Dates. An example is below:  Sun, 10 Sep 2017 19:43:31 UTC (optional)
     * @param xFapiCustomerIpAddress The PSU&#39;s IP address if the PSU is currently logged in with the TPP. (optional)
     * @param xFapiInteractionId An RFC4122 UID used as a correlation id. (optional)
     * @param xCustomerUserAgent Indicates the user-agent that the PSU is using. (optional)
     * @return Default response (status code 201)
     *         or Bad request (status code 400)
     *         or Unauthorized (status code 401)
     *         or Forbidden (status code 403)
     *         or Method Not Allowed (status code 405)
     *         or Not Acceptable (status code 406)
     *         or Unsupported Media Type (status code 415)
     *         or Too Many Requests (status code 429)
     *         or Internal Server Error (status code 500)
     */
    @ApiOperation(value = "Confirm availability of funds for a VRP", nickname = "domesticVrpConsentsFundsConfirmation", notes = "Confirm availability of funds for a VRP", response = OBVRPFundsConfirmationResponse.class, authorizations = {
        @Authorization(value = "TPPOAuth2Security", scopes = {
            @AuthorizationScope(scope = "payments", description = "Generic payment scope") })
         }, tags={ "Domestic VRP Consents", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Default response", response = OBVRPFundsConfirmationResponse.class),
        @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 403, message = "Forbidden", response = OBErrorResponse1.class),
        @ApiResponse(code = 405, message = "Method Not Allowed"),
        @ApiResponse(code = 406, message = "Not Acceptable"),
        @ApiResponse(code = 415, message = "Unsupported Media Type"),
        @ApiResponse(code = 429, message = "Too Many Requests"),
        @ApiResponse(code = 500, message = "Internal Server Error", response = OBErrorResponse1.class) })
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/domestic-vrp-consents/{ConsentId}/funds-confirmation",
        produces = { "application/json; charset=utf-8", "application/json", "application/jose+jwe" },
        consumes = { "application/json; charset=utf-8", "application/json", "application/jose+jwe" }
    )
    ResponseEntity<OBVRPFundsConfirmationResponse> domesticVrpConsentsFundsConfirmation(
            @ApiParam(value = "ConsentId",required=true) @PathVariable("ConsentId") String consentId,
            @ApiParam(value = "An Authorisation Token as per https://tools.ietf.org/html/rfc6750" ,required=true) @RequestHeader(value="Authorization", required=true) String authorization,
            @ApiParam(value = "A detached JWS signature of the body of the payload." ,required=true) @RequestHeader(value="x-jws-signature", required=true) String xJwsSignature,
            @ApiParam(value = "Default" ,required=true )  @Valid @RequestBody OBVRPFundsConfirmationRequest obVRPFundsConfirmationRequest,
            @ApiParam(value = "The time when the PSU last logged in with the TPP.  All dates in the HTTP headers are represented as RFC 7231 Full Dates. An example is below:  Sun, 10 Sep 2017 19:43:31 UTC" ) @RequestHeader(value="x-fapi-auth-date", required=false) String xFapiAuthDate,
            @ApiParam(value = "The PSU's IP address if the PSU is currently logged in with the TPP." ) @RequestHeader(value="x-fapi-customer-ip-address", required=false) String xFapiCustomerIpAddress,
            @ApiParam(value = "An RFC4122 UID used as a correlation id." ) @RequestHeader(value="x-fapi-interaction-id", required=false) String xFapiInteractionId,
            @ApiParam(value = "Indicates the user-agent that the PSU is using." ) @RequestHeader(value="x-customer-user-agent", required=false) String xCustomerUserAgent,
            HttpServletRequest request,
            Principal principal) throws OBErrorResponseException;

}
