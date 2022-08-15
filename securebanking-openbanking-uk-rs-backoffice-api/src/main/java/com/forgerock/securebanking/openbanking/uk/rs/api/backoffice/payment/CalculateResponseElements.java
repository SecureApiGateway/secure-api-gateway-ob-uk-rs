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
package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment;

import com.forgerock.securebanking.openbanking.uk.error.OBErrorResponseException;
import com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.swagger.SwaggerApiTags;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.org.openbanking.datamodel.error.OBErrorResponse1;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Api(tags = {SwaggerApiTags.BACKOFFICE})
@RequestMapping({"/backoffice/payment-consent"})
public interface CalculateResponseElements {

    @ApiOperation(value = "Calculate payment consent response elements", response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Response elements successfully calculated", response = String.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 403, message = "Validation failed", response = OBErrorResponse1.class)
    })
    @RequestMapping(value = "/calculate-elements",
            produces = {"application/json; charset=utf-8"},
            method = RequestMethod.POST)
    ResponseEntity calculateElements(
            @ApiParam(value = "Create an Account Access Consents", required = true)
            @Valid
            @RequestBody String body,

            @ApiParam(value = "Consent type", required = true)
            @RequestParam(value = "intent") String intent,

            @ApiParam(value = "Api version", required = true)
            @RequestParam(value = "version") String version,

            @ApiParam(value = "The unique id of the ASPSP to which the request is issued. The unique id will be issued by OB.", required = true)
            @RequestHeader(value = "x-fapi-financial-id") String xFapiFinancialId,

            @ApiParam(value = "The time when the PSU last logged in with the TPP.  All dates in the HTTP headers are represented as RFC 7231 Full Dates. An example is below:  Sun, 10 Sep 2017 19:43:31 UTC")
            @RequestHeader(value = "x-fapi-auth-date", required = false) String xFapiAuthDate,

            @ApiParam(value = "The PSU's IP address if the PSU is currently logged in with the TPP.")
            @RequestHeader(value = "x-fapi-customer-ip-address", required = false) String xFapiCustomerIpAddress,

            @ApiParam(value = "An RFC4122 UID used as a correlation id.")
            @RequestHeader(value = "x-fapi-interaction-id", required = false) String xFapiInteractionId,

            HttpServletRequest request
    ) throws OBErrorResponseException;
}
