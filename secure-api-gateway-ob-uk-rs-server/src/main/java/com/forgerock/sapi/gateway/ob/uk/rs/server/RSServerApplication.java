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
package com.forgerock.sapi.gateway.ob.uk.rs.server;

import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.MongoRepoPackageMarker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.CloudClientModuleConfiguration;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientConfiguration;

@SpringBootApplication
@Import({CloudClientModuleConfiguration.class, ConsentStoreClientConfiguration.class})
@EnableMongoRepositories(basePackageClasses = MongoRepoPackageMarker.class)
@EnableMongoAuditing
public class RSServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RSServerApplication.class, args);
    }
}
