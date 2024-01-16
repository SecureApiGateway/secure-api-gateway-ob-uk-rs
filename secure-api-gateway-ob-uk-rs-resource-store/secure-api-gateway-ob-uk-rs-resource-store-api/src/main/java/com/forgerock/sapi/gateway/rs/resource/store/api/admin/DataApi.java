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
package com.forgerock.sapi.gateway.rs.resource.store.api.admin;

import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.rs.resource.store.datamodel.account.FRAccountData;
import com.forgerock.sapi.gateway.rs.resource.store.datamodel.user.FRUserData;
import io.swagger.annotations.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Api(tags = "Data API", description = "the internal Data API")
@RequestMapping("/admin/data")
public interface DataApi {

    @ApiOperation(value = "Returns all Account data", nickname = "exportAccountData",
            notes = "Returns a paginated list of Account data", response = FRAccountData.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Data returned successfully", response = FRAccountData.class),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 429, message = "Too Many Requests"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @RequestMapping(value = "/account", method = RequestMethod.GET)
    ResponseEntity<Page<FRAccountData>> exportAccountData(
            @ApiParam(value = "Pageable", required = true)
            @PageableDefault Pageable pageable
    );

    @ApiOperation(value = "User has data", nickname = "has-data",
            notes = "Determines if a user has data in Mongo DB", response = Boolean.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The user has data", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 429, message = "Too Many Requests"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @RequestMapping(value = "/user/has-data", method = RequestMethod.GET)
    ResponseEntity<Boolean> hasData(
            @ApiParam(value = "UserId", required = true)
            @RequestParam("userId") String userId
    );

    @ApiOperation(value = "Export User data", nickname = "exportUserData",
            notes = "Returns all data for a user", response = FRUserData.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the data for a User", response = FRUserData.class),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 429, message = "Too Many Requests"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    ResponseEntity<FRUserData> exportUserData(
            @ApiParam(value = "UserId", required = true)
            @RequestParam("userId") String userId
    );

    @ApiOperation(value = "Update User data", nickname = "updateUserData",
            notes = "Updates a user's data", response = FRUserData.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The user's data was updated", response = FRUserData.class),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 429, message = "Too Many Requests"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @RequestMapping(value = "/user", method = RequestMethod.PUT)
    ResponseEntity updateUserData(
            @ApiParam(value = "UserData", required = true)
            @RequestBody FRUserData userData
    );

    @ApiOperation(value = "Import User data", nickname = "importUserData",
            notes = "Creates data for a user in Mongo DB", response = FRUserData.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The user's data was created", response = FRUserData.class),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 429, message = "Too Many Requests"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @RequestMapping(value = "/user", method = RequestMethod.POST)
    ResponseEntity importUserData(
            @ApiParam(value = "UserData", required = true)
            @RequestBody FRUserData userData
    );

    @ApiOperation(value = "Delete User data", nickname = "deleteUserData",
            notes = "Deletes a user's data from Mongo DB", response = Boolean.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The user's data was deleted", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 429, message = "Too Many Requests"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @RequestMapping(value = "/user", method = RequestMethod.DELETE)
    ResponseEntity<Boolean> deleteUserData(
            @ApiParam(value = "UserName", required = true)
            @RequestParam("userName") String userName
    ) throws ExceptionClient;
}
