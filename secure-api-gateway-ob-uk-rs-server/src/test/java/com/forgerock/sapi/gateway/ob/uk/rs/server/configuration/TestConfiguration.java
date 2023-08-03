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
package com.forgerock.sapi.gateway.ob.uk.rs.server.configuration;

import javax.annotation.PostConstruct;

import org.joda.time.DateTimeZone;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfiguration {

    @PostConstruct
    void postConstruct() {
        // This is needed when decoding Mongo dates to ensure they are in UTC otherwise we get test failures when
        // doing equality checks on OB data-model objects that include dates when the local timezone is not UTC.
        DateTimeZone.setDefault(DateTimeZone.UTC);
    }
}
