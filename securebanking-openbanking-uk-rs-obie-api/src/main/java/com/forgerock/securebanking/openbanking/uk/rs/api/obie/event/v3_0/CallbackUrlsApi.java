/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.event.v3_0;

import com.forgerock.securebanking.openbanking.uk.error.OBErrorResponseException;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.org.openbanking.datamodel.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.event.OBCallbackUrl1;
import uk.org.openbanking.datamodel.event.OBCallbackUrlResponse1;
import uk.org.openbanking.datamodel.event.OBCallbackUrlsResponse1;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;

@Api(value = "callback-urls", description = "the event notification callback urls API")
@RequestMapping(value = "/open-banking/v3.0")
public interface CallbackUrlsApi {

    String BASE_PATH = "/callback-urls";

    @ApiOperation(value = "Create a callback URL", nickname = "createCallbackURL", notes = "", response = OBCallbackUrlResponse1.class, authorizations = {
            @Authorization(value = "TPPOAuth2Security", scopes = {
                    @AuthorizationScope(scope = "accounts", description = "Accounts scope"),
                    @AuthorizationScope(scope = "payments", description = "Payments  scope"),
                    @AuthorizationScope(scope = "fundsconfirmations", description = "Funds Confirmations scope")
            })
    }, tags = {"Callback URLs"})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Callback URLs Created", response = OBCallbackUrlResponse1.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 409, message = "Conflict"), // Note: Not in swagger spec but mentioned directly here: https://openbanking.atlassian.net/wiki/spaces/DZ/pages/645367055/Event+Notification+API+Specification+-+v3.0#EventNotificationAPISpecification-v3.0-POST/callback-urls
            @ApiResponse(code = 415, message = "Unsupported Media Type"),
            @ApiResponse(code = 429, message = "Too Many Requests"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = OBErrorResponse1.class)})

    @RequestMapping(value = BASE_PATH,
            produces = {"application/json; charset=utf-8"},
            consumes = {"application/json; charset=utf-8"},
            method = RequestMethod.POST)
    ResponseEntity<OBCallbackUrlResponse1> createCallbackUrls(
            @ApiParam(value = "Default", required = true)
            @Valid
            @RequestBody OBCallbackUrl1 obCallbackUrl1,

            @ApiParam(value = "The unique id of the ASPSP to which the request is issued. The unique id will be issued by OB.", required = true)
            @RequestHeader(value = "x-fapi-financial-id", required = true) String xFapiFinancialId,

            @ApiParam(value = "An Authorisation Token as per https://tools.ietf.org/html/rfc6750", required = true)
            @RequestHeader(value = "Authorization", required = true) String authorization,

            @ApiParam(value = "Header containing a detached JWS signature of the body of the payload.", required = true)
            @RequestHeader(value = "x-jws-signature", required = false) String xJwsSignature,

            @ApiParam(value = "An RFC4122 UID used as a correlation id.")
            @RequestHeader(value = "x-fapi-interaction-id", required = false) String xFapiInteractionId,

            @ApiParam(value = "The PISP TPP ID (otherwise referred to as the Client ID")
            @RequestHeader(value = "x-ob-tpp-id", required = true) String tppId,

            HttpServletRequest request,

            Principal principal
    ) throws OBErrorResponseException;

    @ApiOperation(value = "Read all callback URLs", nickname = "readCallbackUrls", notes = "", response = OBCallbackUrlsResponse1.class, authorizations = {
            @Authorization(value = "TPPOAuth2Security", scopes = {
                    @AuthorizationScope(scope = "accounts", description = "Accounts scope"),
                    @AuthorizationScope(scope = "payments", description = "Payments  scope"),
                    @AuthorizationScope(scope = "fundsconfirmations", description = "Funds Confirmations scope")
            })
    }, tags = {"Callback URLs"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback URLs Read", response = OBCallbackUrlResponse1.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 429, message = "Too Many Requests"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = OBErrorResponse1.class)})

    @RequestMapping(value = BASE_PATH,
            produces = {"application/json; charset=utf-8"},
            method = RequestMethod.GET)
    ResponseEntity<OBCallbackUrlsResponse1> readCallBackUrls(
            @ApiParam(value = "The unique id of the ASPSP to which the request is issued. The unique id will be issued by OB.", required = false)
            @RequestHeader(value = "x-fapi-financial-id", required = true) String xFapiFinancialId,

            @ApiParam(value = "An Authorisation Token as per https://tools.ietf.org/html/rfc6750", required = true)
            @RequestHeader(value = "Authorization", required = true) String authorization,

            @ApiParam(value = "An RFC4122 UID used as a correlation id.")
            @RequestHeader(value = "x-fapi-interaction-id", required = false) String xFapiInteractionId,

            @ApiParam(value = "The PISP TPP ID (otherwise referred to as the Client ID")
            @RequestHeader(value = "x-ob-tpp-id", required = true) String tppId,

            HttpServletRequest request,

            Principal principal
    ) throws OBErrorResponseException;

    @ApiOperation(value = "Amend a callback URI", nickname = "amendCallbackURL", notes = "", response = OBCallbackUrlResponse1.class, authorizations = {
            @Authorization(value = "TPPOAuth2Security", scopes = {
                    @AuthorizationScope(scope = "accounts", description = "Accounts scope"),
                    @AuthorizationScope(scope = "payments", description = "Payments  scope"),
                    @AuthorizationScope(scope = "fundsconfirmations", description = "Funds Confirmations scope")
            })
    }, tags = {"Callback URLs"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback URLs Amended", response = OBCallbackUrlResponse1.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 429, message = "Too Many Requests"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = OBErrorResponse1.class)})

    @RequestMapping(value = BASE_PATH + "/{CallbackUrlId}",
            produces = {"application/json; charset=utf-8"},
            consumes = {"application/json; charset=utf-8"},
            method = RequestMethod.PUT)
    ResponseEntity<OBCallbackUrlResponse1> updateCallbackUrl(
            @ApiParam(value = "CallbackUrlId", required = true)
            @PathVariable("CallbackUrlId") String callbackUrlId,

            @ApiParam(value = "Default", required = true)
            @Valid
            @RequestBody OBCallbackUrl1 obCallbackUrl1,

            @ApiParam(value = "The unique id of the ASPSP to which the request is issued. The unique id will be issued by OB.", required = false)
            @RequestHeader(value = "x-fapi-financial-id", required = true) String xFapiFinancialId,

            @ApiParam(value = "An Authorisation Token as per https://tools.ietf.org/html/rfc6750", required = true)
            @RequestHeader(value = "Authorization", required = true) String authorization,

            @ApiParam(value = "Header containing a detached JWS signature of the body of the payload.", required = true)
            @RequestHeader(value = "x-jws-signature", required = false) String xJwsSignature,

            @ApiParam(value = "An RFC4122 UID used as a correlation id.")
            @RequestHeader(value = "x-fapi-interaction-id", required = false) String xFapiInteractionId,

            @ApiParam(value = "The PISP TPP ID (otherwise referred to as the Client ID")
            @RequestHeader(value = "x-ob-tpp-id", required = false) String tppId,

            HttpServletRequest request,

            Principal principal
    ) throws OBErrorResponseException;

    @ApiOperation(value = "Delete a callback URI", nickname = "deleteCallbackURL", notes = "", authorizations = {
            @Authorization(value = "TPPOAuth2Security", scopes = {
                    @AuthorizationScope(scope = "accounts", description = "Accounts scope"),
                    @AuthorizationScope(scope = "payments", description = "Payments  scope"),
                    @AuthorizationScope(scope = "fundsconfirmations", description = "Funds Confirmations scope")
            })
    }, tags = {"Callback URLs"})
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Callback URLs Deleted", response = OBCallbackUrlResponse1.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 429, message = "Too Many Requests"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = OBErrorResponse1.class)})

    @RequestMapping(value = BASE_PATH + "/{CallbackUrlId}",
            method = RequestMethod.DELETE)
    ResponseEntity deleteCallbackUrl(
            @ApiParam(value = "CallbackUrlId", required = true)
            @PathVariable("CallbackUrlId") String callbackUrlId,

            @ApiParam(value = "The unique id of the ASPSP to which the request is issued. The unique id will be issued by OB.", required = false)
            @RequestHeader(value = "x-fapi-financial-id", required = true) String xFapiFinancialId,

            @ApiParam(value = "An Authorisation Token as per https://tools.ietf.org/html/rfc6750", required = true)
            @RequestHeader(value = "Authorization", required = true) String authorization,

            @ApiParam(value = "An RFC4122 UID used as a correlation id.")
            @RequestHeader(value = "x-fapi-interaction-id", required = false) String xFapiInteractionId,

            @ApiParam(value = "The PISP TPP ID (otherwise referred to as the Client ID")
            @RequestHeader(value = "x-ob-tpp-id", required = false) String tppId,

            HttpServletRequest request,

            Principal principal
    ) throws OBErrorResponseException;

}
