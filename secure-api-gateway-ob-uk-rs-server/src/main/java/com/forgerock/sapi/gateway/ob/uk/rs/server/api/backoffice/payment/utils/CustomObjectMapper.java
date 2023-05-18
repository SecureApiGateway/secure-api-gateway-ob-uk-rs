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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.backoffice.payment.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.joda.time.DateTime;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.org.openbanking.jackson.DateTimeDeserializer;
import uk.org.openbanking.jackson.DateTimeSerializer;

import java.util.TimeZone;

public class CustomObjectMapper {
    private static CustomObjectMapper customObjectMapper;
    private static ObjectMapper objectMapper;

    private CustomObjectMapper() {
        Jackson2ObjectMapperBuilderCustomizer customizer =
                jacksonObjectMapperBuilder -> {
                    jacksonObjectMapperBuilder.timeZone(TimeZone.getDefault());
                    jacksonObjectMapperBuilder.featuresToEnable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
                    jacksonObjectMapperBuilder.modules(new JodaModule());
                    jacksonObjectMapperBuilder.deserializerByType(DateTime.class, new DateTimeDeserializer());
                    jacksonObjectMapperBuilder.serializerByType(DateTime.class, new DateTimeSerializer(DateTime.class));
                    jacksonObjectMapperBuilder.serializationInclusion(JsonInclude.Include.ALWAYS);
                };

        Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
        customizer.customize(builder);

        objectMapper = builder.build();
        objectMapper.setNodeFactory(JsonNodeFactory.withExactBigDecimals(true));
    }

    public static CustomObjectMapper getCustomObjectMapper() {
        if (customObjectMapper == null) {
            customObjectMapper = new CustomObjectMapper();
        }

        return customObjectMapper;
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

}
