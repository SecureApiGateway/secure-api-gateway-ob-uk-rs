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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.discovery;

import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBGroupName;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.org.openbanking.datamodel.v3.discovery.OBDiscovery;
import uk.org.openbanking.datamodel.v3.discovery.OBDiscoveryAPI;
import uk.org.openbanking.datamodel.v3.discovery.OBDiscoveryResponse;

import java.util.Map;

/**
 * Controller for the "Discovery" endpoint. Provides a list of URLs for the Open Banking Read/Write API that are
 * supported by a particular deployed instance.
 *
 * <p>
 * For example, a TPP may use this endpoint to see which specific versions and endpoints of the Read/Write API are
 * supported.
 */
@Controller
@Slf4j
@Api(tags = "Discovery", description = "the discovery API")
public class DiscoveryController {

    private final DiscoveryApiService discoveryAPIsService;

    private final DiscoveryApiConfigurationProperties discoveryAPIsConfigurationProperties;

    public DiscoveryController(DiscoveryApiService discoveryAPIsService, DiscoveryApiConfigurationProperties discoveryAPIsConfigurationProperties) {
        this.discoveryAPIsService = discoveryAPIsService;
        this.discoveryAPIsConfigurationProperties = discoveryAPIsConfigurationProperties;
    }

    @ApiOperation(
            value = "Discover the Read/Write API",
            notes = "Get all the supported endpoints (versions and paths) for the Open Banking Read/Write API.",
            response = OBDiscoveryResponse.class,
            tags = {"Discovery",}
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Discovery endpoints",
                    response = String.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = Void.class)
    })

    @RequestMapping(
            value = "/open-banking/discovery",
            produces = {"application/json; charset=utf-8"},
            method = RequestMethod.GET
    )
    public ResponseEntity<OBDiscoveryResponse> getDiscoveryResponse() {
        OBDiscoveryResponse response = new OBDiscoveryResponse();
        OBDiscovery discovery = new OBDiscovery();
        discovery.setFinancialId(discoveryAPIsConfigurationProperties.getFinancialId());

        for (Map.Entry<OBGroupName, Map<String, OBDiscoveryAPI>> byGroup : discoveryAPIsService.getDiscoveryApis().entrySet()) {
            if (byGroup.getValue().isEmpty()) {
                continue;
            }
            for (OBDiscoveryAPI obDiscoveryAPI : byGroup.getValue().values()) {
                switch (byGroup.getKey()) {
                    case AISP:
                        discovery.addAccountAndTransactionAPI(obDiscoveryAPI);
                        break;
                    case PISP:
                        discovery.addPaymentInitiationAPI(obDiscoveryAPI);
                        break;
                    case CBPII:
                        discovery.addFundsConfirmationAPI(obDiscoveryAPI);
                        break;
                    case EVENT:
                        discovery.addEventNotificationAPI(obDiscoveryAPI);
                        break;
                }
            }
        }
        return ResponseEntity.ok(response.data(discovery));
    }
}
