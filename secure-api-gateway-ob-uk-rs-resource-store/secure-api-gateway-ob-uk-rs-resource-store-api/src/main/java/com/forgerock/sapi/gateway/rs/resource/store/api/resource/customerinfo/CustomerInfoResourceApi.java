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
package com.forgerock.sapi.gateway.rs.resource.store.api.resource.customerinfo;


import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRAccountWithBalance;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.customerinfo.FRCustomerInfo;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.org.openbanking.datamodel.error.OBErrorResponse1;

import java.util.List;

/**
 * "Resources" endpoints related to Accounts, which are not part of the official Open Banking Read/Write API.
 */
@Api(value = "resources", description = "the Back Office Accounts API")
@RequestMapping(value = "/resources")
public interface CustomerInfoResourceApi {

    /**
     * Retrieves a user's bank customer information details.
     *
     * @param userId The ID of the user who owns the account
     * @return a {@link List} of {@link FRAccountWithBalance} instances, or an empty list.
     */
    @ApiOperation(value = "Get User Customer information",
            nickname = "getCustomerInformation",
            notes = "", response = FRCustomerInfo.class,
            tags = {"Get User Customer info",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User Customer information", response = FRCustomerInfo.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden", response = OBErrorResponse1.class),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 429, message = "Too Many Requests"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = OBErrorResponse1.class)})
    @RequestMapping(value = "/customerinfo/findByUserId",
            produces = {"application/json; charset=utf-8"},
            method = RequestMethod.GET)
    ResponseEntity<FRCustomerInfo> getCustomerInformation(
            @ApiParam(value = "The ID of the PSU who owns the customer information.")
            @RequestParam(value = "userId") String userId
    );
}
