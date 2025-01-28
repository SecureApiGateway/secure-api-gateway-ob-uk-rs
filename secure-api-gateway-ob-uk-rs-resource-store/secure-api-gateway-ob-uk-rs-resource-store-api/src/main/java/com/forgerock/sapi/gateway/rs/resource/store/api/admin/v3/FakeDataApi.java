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
package com.forgerock.sapi.gateway.rs.resource.store.api.admin.v3;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.rs.resource.store.datamodel.user.v3.FRUserData;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Api(tags = "Fake Data API", description = "API for generating 'fake' data within Mongo DB")
@RequestMapping(value = "/admin/fake-data")
public interface FakeDataApi {

    @ApiOperation(value = "Generate Fake Data", nickname = "generateFakeData", notes = "Generates fake data", response = FRUserData.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Data generated successfully", response = FRUserData.class),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 429, message = "Too Many Requests"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @RequestMapping(value = "/generate",
            method = RequestMethod.POST)
    ResponseEntity generateFakeData(
            @ApiParam(value = "UserId", required = true)
            @RequestParam("userId") String userId,
            @ApiParam(value = "Username", required = true)
            @RequestParam("username") String username,
            @ApiParam(value = "Profile")
            @RequestParam(name = "profile", required = false) String profile
    ) throws OBErrorException;
}
