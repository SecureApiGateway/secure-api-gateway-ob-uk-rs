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
package com.forgerock.securebanking.rs.platform.client.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.net.URI;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "identity-platform.client")
@Data
public class ConfigurationPropertiesClient {
    private String igFqdn;
    private String identityPlatformFqdn;
    @Value("${scheme:https}")
    private String scheme;
    /*
     * Spring maps the properties, the keys from file will be the map keys
     * @see: application-test.yml
     * case-insensitive map's keys
     * @Use pattern: contextMap.get(http-verb-any-case)
     * @Use: contextAccountsConsent.get("GeT")
     */
    private Map<String, String> contextsRepoConsent = new LinkedCaseInsensitiveMap<>();

    private static final String _delimiter = "://";

    public String getIgFqdnURIAsString() {
        return String.join(_delimiter, scheme, igFqdn);
    }

    public URI getIgFqdnURI() {
        return URI.create(String.join(_delimiter, scheme, igFqdn));
    }
}
