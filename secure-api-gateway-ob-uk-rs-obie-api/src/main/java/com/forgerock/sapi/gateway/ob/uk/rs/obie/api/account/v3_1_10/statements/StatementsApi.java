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
/**
 * NOTE: This class is auto generated by the swagger code generator program (2.3.1).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v3_1_10.statements;

import java.time.LocalDateTime;

import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.ApiConstants;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.swagger.SwaggerApiTags;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import uk.org.openbanking.datamodel.v3.account.OBReadStatement2;
import uk.org.openbanking.datamodel.v3.error.OBErrorResponse1;

@Api(tags = {"v3.1.10", SwaggerApiTags.ACCOUNTS_AND_TRANSACTION_TAG})
@RequestMapping(value = "/open-banking/v3.1.10/aisp")
public interface StatementsApi {

    @ApiOperation(value = "Get Account Statement", nickname = "getAccountStatement", notes = "Get Statement related to an account",
            response = OBReadStatement2.class, authorizations = {
            @Authorization(value = "PSUOAuth2Security", scopes = {
                    @AuthorizationScope(scope = "accounts", description = "Ability to read Accounts information")
            })
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Account Statement successfully retrieved", response = OBReadStatement2.class),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 429, message = "Too Many Requests"),
            @ApiResponse(code = 500, message = "Internal Server Error")})

    @RequestMapping(value = "/accounts/{AccountId}/statements/{StatementId}",
            produces = {"application/json; charset=utf-8", "application/jose+jwe"},
            method = RequestMethod.GET)
    ResponseEntity<OBReadStatement2> getAccountStatement(
            @ApiParam(value = "StatementId", required = true)
            @PathVariable("StatementId") String statementId,

            @ApiParam(value = "AccountId", required = true)
            @PathVariable("AccountId") String accountId,

            @ApiParam(value = "Page number.", required = false, defaultValue = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,

            @ApiParam(value = "An Authorisation Token as per https://tools.ietf.org/html/rfc6750", required = true)
            @RequestHeader(value = "Authorization", required = true) String authorization,

            @ApiParam(value = "The time when the PSU last logged in with the TPP.  All dates in the HTTP headers are represented as RFC 7231 Full Dates. An example is below:  Sun, 10 Sep 2017 19:43:31 UTC")
            @RequestHeader(value = "x-fapi-auth-date", required = false)
            @DateTimeFormat(pattern = ApiConstants.HTTP_DATE_FORMAT) String xFapiAuthDate,

            @ApiParam(value = "The PSU's IP address if the PSU is currently logged in with the TPP.")
            @RequestHeader(value = "x-fapi-customer-ip-address", required = false) String xFapiCustomerIpAddress,

            @ApiParam(value = "An RFC4122 UID used as a correlation id.")
            @RequestHeader(value = "x-fapi-interaction-id", required = false) String xFapiInteractionId,

            @ApiParam(value = "Indicates the user-agent that the PSU is using.")
            @RequestHeader(value = "x-customer-user-agent", required = false) String xCustomerUserAgent,

            @ApiParam(value = "openbanking_intent_id from the access_token")
            @RequestHeader(value = "x-intent-id") String consentId,

            @ApiParam(value = "OAuth2.0 client_id of the ApiClient making the request")
            @RequestHeader(value = "x-api-client-id") String apiClientId
    ) throws OBErrorException;


    @ApiOperation(value = "Get Account Statement File", nickname = "getAccountStatementFile", notes = "Get Statement File related to an account",
            response = Resource.class, authorizations = {
            @Authorization(value = "PSUOAuth2Security", scopes = {
                    @AuthorizationScope(scope = "accounts", description = "Ability to read Accounts information")
            })
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Account Statement File successfully retrieved", response = Resource.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 429, message = "Too Many Requests"),
            @ApiResponse(code = 500, message = "Internal Server Error")})

    @RequestMapping(value = "/accounts/{AccountId}/statements/{StatementId}/file",
            produces = {"*/*"},
            method = RequestMethod.GET)
    ResponseEntity<Resource> getAccountStatementFile(
            @ApiParam(value = "StatementId", required = true)
            @PathVariable("StatementId") String statementId,

            @ApiParam(value = "AccountId", required = true)
            @PathVariable("AccountId") String accountId,

            @ApiParam(value = "Page number.", required = false, defaultValue = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,

            @ApiParam(value = "An Authorisation Token as per https://tools.ietf.org/html/rfc6750", required = true)
            @RequestHeader(value = "Authorization", required = true) String authorization,

            @ApiParam(value = "The time when the PSU last logged in with the TPP.  All dates in the HTTP headers are represented as RFC 7231 Full Dates. An example is below:  Sun, 10 Sep 2017 19:43:31 UTC")
            @RequestHeader(value = "x-fapi-auth-date", required = false)
            @DateTimeFormat(pattern = ApiConstants.HTTP_DATE_FORMAT) String xFapiAuthDate,

            @ApiParam(value = "The PSU's IP address if the PSU is currently logged in with the TPP.")
            @RequestHeader(value = "x-fapi-customer-ip-address", required = false) String xFapiCustomerIpAddress,

            @ApiParam(value = "An RFC4122 UID used as a correlation id.")
            @RequestHeader(value = "x-fapi-interaction-id", required = false) String xFapiInteractionId,

            @ApiParam(value = "HTTP Accept header defining what files will be accepted.", required = true)
            @RequestHeader(value = "Accept", required = true) String accept,

            @ApiParam(value = "openbanking_intent_id from the access_token")
            @RequestHeader(value = "x-intent-id") String consentId,

            @ApiParam(value = "OAuth2.0 client_id of the ApiClient making the request")
            @RequestHeader(value = "x-api-client-id") String apiClientId
    ) throws OBErrorException, OBErrorResponseException;


    @ApiOperation(value = "Get Account Statements", nickname = "getAccountStatements", notes = "Get Statements related to an account",
            response = OBReadStatement2.class, authorizations = {
            @Authorization(value = "PSUOAuth2Security", scopes = {
                    @AuthorizationScope(scope = "accounts", description = "Ability to read Accounts information")
            })
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Account Statements successfully retrieved", response = OBReadStatement2.class),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 429, message = "Too Many Requests"),
            @ApiResponse(code = 500, message = "Internal Server Error")})

    @RequestMapping(value = "/accounts/{AccountId}/statements",
            produces = {"application/json; charset=utf-8", "application/jose+jwe"},
            method = RequestMethod.GET)
    ResponseEntity<OBReadStatement2> getAccountStatements(
            @ApiParam(value = "AccountId", required = true)
            @PathVariable("AccountId") String accountId,

            @ApiParam(value = "Page number.", required = false, defaultValue = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,

            @ApiParam(value = "An Authorisation Token as per https://tools.ietf.org/html/rfc6750", required = true)
            @RequestHeader(value = "Authorization", required = true) String authorization,

            @ApiParam(value = "The time when the PSU last logged in with the TPP.  All dates in the HTTP headers are represented as RFC 7231 Full Dates. An example is below:  Sun, 10 Sep 2017 19:43:31 UTC")
            @RequestHeader(value = "x-fapi-auth-date", required = false)
            @DateTimeFormat(pattern = ApiConstants.HTTP_DATE_FORMAT) String xFapiAuthDate,

            @ApiParam(value = "The UTC ISO 8601 Date Time to filter statements FROM NB Time component is optional - set to 00:00:00 for just Date.   The parameter must NOT have a timezone set")
            @RequestParam(value = ApiConstants.ParametersFieldName.FROM_STATEMENT_DATE_TIME, required = false)
            @DateTimeFormat(pattern = ApiConstants.STATEMENT_TIME_DATE_FORMAT) LocalDateTime fromStatementDateTime,

            @ApiParam(value = "The UTC ISO 8601 Date Time to filter statements TO NB Time component is optional - set to 00:00:00 for just Date.   The parameter must NOT have a timezone set")
            @RequestParam(value = ApiConstants.ParametersFieldName.TO_STATEMENT_DATE_TIME, required = false)
            @DateTimeFormat(pattern = ApiConstants.STATEMENT_TIME_DATE_FORMAT) LocalDateTime toStatementDateTime,

            @ApiParam(value = "The PSU's IP address if the PSU is currently logged in with the TPP.")
            @RequestHeader(value = "x-fapi-customer-ip-address", required = false) String xFapiCustomerIpAddress,

            @ApiParam(value = "An RFC4122 UID used as a correlation id.")
            @RequestHeader(value = "x-fapi-interaction-id", required = false) String xFapiInteractionId,

            @ApiParam(value = "Indicates the user-agent that the PSU is using.")
            @RequestHeader(value = "x-customer-user-agent", required = false) String xCustomerUserAgent,

            @ApiParam(value = "openbanking_intent_id from the access_token")
            @RequestHeader(value = "x-intent-id") String consentId,

            @ApiParam(value = "OAuth2.0 client_id of the ApiClient making the request")
            @RequestHeader(value = "x-api-client-id") String apiClientId
    ) throws OBErrorException;


    @ApiOperation(value = "Get Statements", nickname = "getStatements", notes = "Get Statements", response = OBReadStatement2.class, authorizations = {
            @Authorization(value = "PSUOAuth2Security", scopes = {
                    @AuthorizationScope(scope = "accounts", description = "Ability to read Accounts information")
            })
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Statements successfully retrieved", response = OBReadStatement2.class),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 429, message = "Too Many Requests"),
            @ApiResponse(code = 500, message = "Internal Server Error")})

    @RequestMapping(value = "/statements",
            produces = {"application/json; charset=utf-8", "application/jose+jwe"},
            method = RequestMethod.GET)
    ResponseEntity<OBReadStatement2> getStatements(
            @ApiParam(value = "Page number.", required = false, defaultValue = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,

            @ApiParam(value = "An Authorisation Token as per https://tools.ietf.org/html/rfc6750", required = true)
            @RequestHeader(value = "Authorization", required = true) String authorization,

            @ApiParam(value = "The time when the PSU last logged in with the TPP.  All dates in the HTTP headers are represented as RFC 7231 Full Dates. An example is below:  Sun, 10 Sep 2017 19:43:31 UTC")
            @RequestHeader(value = "x-fapi-auth-date", required = false)
            @DateTimeFormat(pattern = ApiConstants.HTTP_DATE_FORMAT) String xFapiAuthDate,

            @ApiParam(value = "The UTC ISO 8601 Date Time to filter statements FROM NB Time component is optional - set to 00:00:00 for just Date.   The parameter must NOT have a timezone set")
            @RequestParam(value = ApiConstants.ParametersFieldName.FROM_STATEMENT_DATE_TIME, required = false)
            @DateTimeFormat(pattern = ApiConstants.STATEMENT_TIME_DATE_FORMAT) LocalDateTime fromStatementDateTime,

            @ApiParam(value = "The UTC ISO 8601 Date Time to filter statements TO NB Time component is optional - set to 00:00:00 for just Date.   The parameter must NOT have a timezone set")
            @RequestParam(value = ApiConstants.ParametersFieldName.TO_STATEMENT_DATE_TIME, required = false)
            @DateTimeFormat(pattern = ApiConstants.STATEMENT_TIME_DATE_FORMAT) LocalDateTime toStatementDateTime,

            @ApiParam(value = "The PSU's IP address if the PSU is currently logged in with the TPP.")
            @RequestHeader(value = "x-fapi-customer-ip-address", required = false) String xFapiCustomerIpAddress,

            @ApiParam(value = "An RFC4122 UID used as a correlation id.")
            @RequestHeader(value = "x-fapi-interaction-id", required = false) String xFapiInteractionId,

            @ApiParam(value = "Indicates the user-agent that the PSU is using.")
            @RequestHeader(value = "x-customer-user-agent", required = false) String xCustomerUserAgent,

            @ApiParam(value = "openbanking_intent_id from the access_token")
            @RequestHeader(value = "x-intent-id") String consentId,

            @ApiParam(value = "OAuth2.0 client_id of the ApiClient making the request")
            @RequestHeader(value = "x-api-client-id") String apiClientId
    ) throws OBErrorException;

}
