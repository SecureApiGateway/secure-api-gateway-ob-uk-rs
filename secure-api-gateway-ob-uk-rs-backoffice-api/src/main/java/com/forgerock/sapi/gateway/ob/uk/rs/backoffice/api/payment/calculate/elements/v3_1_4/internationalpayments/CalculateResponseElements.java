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
package com.forgerock.sapi.gateway.ob.uk.rs.backoffice.api.payment.calculate.elements.v3_1_4.internationalpayments;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.rs.backoffice.api.swagger.SwaggerApiTags;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.org.openbanking.datamodel.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalConsent5;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalConsentResponse5;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Api(tags = {"v3.1.4", SwaggerApiTags.BACKOFFICE})
@RequestMapping({"/backoffice/v3.1.4/international-payment-consents"})
public interface CalculateResponseElements {

    @ApiOperation(value = "Calculate payment consent response elements", response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Response elements successfully calculated", response = String.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Validation failed", response = OBErrorResponse1.class),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 404, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 429, message = "Too Many Requests"),
            @ApiResponse(code = 400, message = "Internal Server Error", response = OBErrorResponse1.class)
    })
    @RequestMapping(value = "/calculate-elements",
            produces = {"application/json; charset=utf-8"},
            method = RequestMethod.POST)
    ResponseEntity<OBWriteInternationalConsentResponse5> calculateElements(
            @ApiParam(value = "Payment Consent OB object request body", required = true)
            @Valid
            @RequestBody OBWriteInternationalConsent5 body,

            @ApiParam(value = "Consent type", required = true)
            @RequestParam(value = "intent") String intent,

            @ApiParam(value = "The time when the PSU last logged in with the TPP.  All dates in the HTTP headers are represented as RFC 7231 Full Dates. An example is below:  Sun, 10 Sep 2017 19:43:31 UTC")
            @RequestHeader(value = "x-fapi-auth-date", required = false) String xFapiAuthDate,

            @ApiParam(value = "The PSU's IP address if the PSU is currently logged in with the TPP.")
            @RequestHeader(value = "x-fapi-customer-ip-address", required = false) String xFapiCustomerIpAddress,

            @ApiParam(value = "An RFC4122 UID used as a correlation id.")
            @RequestHeader(value = "x-fapi-interaction-id", required = false) String xFapiInteractionId,

            HttpServletRequest request
    ) throws OBErrorResponseException;
}
