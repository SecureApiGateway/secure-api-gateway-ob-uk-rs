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
package com.forgerock.sapi.gateway.rs.resource.store.repo;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.MongoRepoPackageMarker;
import com.forgerock.sapi.gateway.uk.common.shared.spring.converter.JodaTimeConverters;

@Configuration
@ComponentScan(basePackageClasses = ResourceStoreRepoConfiguration.class)
@EnableMongoRepositories(basePackageClasses = MongoRepoPackageMarker.class)
@EnableMongoAuditing
public class ResourceStoreRepoConfiguration {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Create MongoCustomConversions instance with Joda Time converters
     */
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        logger.info("Installing joda time converters for Mongo");
        return new MongoCustomConversions(new ArrayList<>(JodaTimeConverters.getConvertersToRegister()));
    }

}
