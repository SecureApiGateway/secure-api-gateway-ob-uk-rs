/**
 * Copyright © 2020 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.discovery;

import com.forgerock.securebanking.openbanking.uk.rs.common.OBApiReference;
import com.forgerock.securebanking.openbanking.uk.common.api.meta.OBGroupName;
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
    private String url;
    private ControllerMethod controllerMethod;
}
