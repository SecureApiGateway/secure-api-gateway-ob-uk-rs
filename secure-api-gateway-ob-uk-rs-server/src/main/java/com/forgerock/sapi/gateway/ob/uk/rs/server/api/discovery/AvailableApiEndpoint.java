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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.discovery;

import com.forgerock.sapi.gateway.ob.uk.rs.server.common.OBApiReference;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBGroupName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class represents an "available" endpoint in the Read/Write API. It is available in the sense that it is
 * has been implemented by this application.
 *
 * <p>
 * If a customer does not support an API endpoint, then they should disable it in their application configuration
 * (which subsequently changes the output of the Discovery endpoint).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableApiEndpoint {
    private OBGroupName groupName;
    private String version;
    private OBApiReference apiReference;

    /**
     * The path portion of the URI that can be used to access this endpoint e.g. /open-banking/v3.1.10/pisp/domestic-payments
     *
     * This needs to be combined with the baseUri used to access this component, this can be done using Spring HATEOAS
     */
    private String uriPath;

    private ControllerMethod controllerMethod;
}
