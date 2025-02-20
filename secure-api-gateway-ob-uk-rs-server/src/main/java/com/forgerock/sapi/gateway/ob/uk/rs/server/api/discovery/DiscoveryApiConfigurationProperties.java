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

import com.forgerock.sapi.gateway.ob.uk.rs.server.common.OBApiReference;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads customer specific config (if applicable) for specifying which of the available APIs should be enabled/disabled.
 */
@Component
@Data
@ConfigurationProperties(prefix = "rs.discovery")
public class DiscoveryApiConfigurationProperties {

    private String financialId;

    public Map<OBApiReference, Boolean> apis = new HashMap<>();

    private Map<String, Boolean> versions = new HashMap<>();

    private Map<String, Map<OBApiReference, Boolean>> versionApiOverrides = new HashMap<>();
}
