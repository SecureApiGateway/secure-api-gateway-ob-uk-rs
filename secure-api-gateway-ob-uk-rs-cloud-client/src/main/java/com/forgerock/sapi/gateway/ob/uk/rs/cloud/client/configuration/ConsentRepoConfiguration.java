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
package com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "consent-repo.client")
@Data
public class ConsentRepoConfiguration {

    private String host;
    private int port;
    private String scheme;
    /*
     * Spring maps the properties, the keys from file will be the map keys
     * @see: application-test.yml
     * case-insensitive map's keys
     * @Use pattern: contextMap.get(http-verb-any-case)
     * @Use: contextAccountsConsent.get("GeT")
     */
    private Map<String, String> contextsRepoConsent = new LinkedCaseInsensitiveMap<>();


    public String getConsentRepoBaseUri() {
        return new StringBuilder(128).append(scheme).append("://").append(host).append(':').append(port).toString();
    }
}
