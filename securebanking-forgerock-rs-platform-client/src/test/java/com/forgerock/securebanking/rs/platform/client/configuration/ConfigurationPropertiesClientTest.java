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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link ConfigurationPropertiesClient}
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ConfigurationPropertiesClient.class}, initializers = ConfigFileApplicationContextInitializer.class)
@EnableConfigurationProperties(value = ConfigurationPropertiesClient.class)
@ActiveProfiles("test")
public class ConfigurationPropertiesClientTest {

    @Autowired
    private ConfigurationPropertiesClient configurationPropertiesClient;

    @Test
    public void shouldHaveAllProperties(){
        assertThat(configurationPropertiesClient.getIgFqdn()).isNotNull();
        assertThat(configurationPropertiesClient.getIdentityPlatformFqdn()).isNotNull();
        assertThat(configurationPropertiesClient.getScheme()).isNotNull();
        assertThat(configurationPropertiesClient.getContextsRepoConsent()).isNotEmpty();
    }
}
