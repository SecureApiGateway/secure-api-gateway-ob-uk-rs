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
package com.forgerock.sapi.gateway.rs.resource.store.api;

import static com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS;
import static com.fasterxml.jackson.databind.MapperFeature.USE_BASE_TYPE_AS_DEFAULT_IMPL;

import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.CloudClientModuleConfiguration;
import com.forgerock.sapi.gateway.rs.resource.store.repo.ResourceStoreRepoConfiguration;

import uk.org.openbanking.jackson.DateTimeDeserializer;
import uk.org.openbanking.jackson.DateTimeSerializer;
import uk.org.openbanking.jackson.LocalDateDeserializer;
import uk.org.openbanking.jackson.LocalDateSerializer;

@SpringBootApplication
@Import({ResourceStoreRepoConfiguration.class, ResourceStoreApiModuleConfiguration.class, CloudClientModuleConfiguration.class})
public class TestApp {

    public static void main(String[] args) {
        SpringApplication.run(TestApp.class, args);
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer objectMapperBuilderCustomizer() {
        return (jacksonObjectMapperBuilder) -> {
            jacksonObjectMapperBuilder.timeZone(TimeZone.getDefault());
            jacksonObjectMapperBuilder.serializationInclusion(Include.NON_NULL);
            jacksonObjectMapperBuilder.featuresToDisable(FAIL_ON_UNKNOWN_PROPERTIES);
            jacksonObjectMapperBuilder.featuresToEnable(USE_BASE_TYPE_AS_DEFAULT_IMPL, USE_BIG_DECIMAL_FOR_FLOATS, WRITE_BIGDECIMAL_AS_PLAIN);
            jacksonObjectMapperBuilder.modules(new JodaModule());
            jacksonObjectMapperBuilder.deserializerByType(DateTime.class, new DateTimeDeserializer());
            jacksonObjectMapperBuilder.serializerByType(DateTime.class, new DateTimeSerializer(DateTime.class));
            jacksonObjectMapperBuilder.deserializerByType(LocalDate.class, new LocalDateDeserializer());
            jacksonObjectMapperBuilder.serializerByType(LocalDate.class, new LocalDateSerializer(LocalDate.class));
        };
    }
}
