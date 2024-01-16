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
package com.forgerock.sapi.gateway.rs.resource.store.api.admin.events;


import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.event.FREventMessages;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;

import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.event.FREventMessageEntity;

import io.swagger.annotations.ApiParam;


@RequestMapping("/admin/data")
public interface DataEventsApi {

    @RequestMapping(value = "/events",
            produces = {"application/json; charset=utf-8"},
            consumes = {"application/json; charset=utf-8"},
            method = RequestMethod.POST)
    ResponseEntity<FREventMessages> importEvents(
            @ApiParam(value = "Default", required = true)
            @Valid
            @RequestBody FREventMessages frEventMessages
    ) throws OBErrorException;

    @RequestMapping(value = "/events",
            produces = {"application/json; charset=utf-8"},
            consumes = {"application/json; charset=utf-8"},
            method = RequestMethod.PUT)
    ResponseEntity<FREventMessages> updateEvents(
            @ApiParam(value = "Default", required = true)
            @Valid
            @RequestBody FREventMessages frEventMessages
    ) throws OBErrorException;

    @RequestMapping(value = "/events",
            produces = {"application/json; charset=utf-8"},
            method = RequestMethod.GET
    )
    ResponseEntity<List<FREventMessageEntity>> exportEvents(
            @ApiParam(value = "Default", required = true)
            @Valid
            @RequestParam String apiClientId
    ) throws OBErrorException;

    @RequestMapping(value = "/events",
            method = RequestMethod.DELETE
    )
    ResponseEntity removeEvents(
            @ApiParam(value = "The client ID", required = true)
            @Valid
            @RequestParam(value = "apiClientId") String apiClientId,
            @ApiParam(value = "Unique identification of event message")
            @RequestParam(value = "jti", required = false) String jti
    ) throws OBErrorException;

}
