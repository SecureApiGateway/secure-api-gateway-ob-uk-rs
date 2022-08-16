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
package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.joda.time.DateTime;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.org.openbanking.jackson.DateTimeDeserializer;
import uk.org.openbanking.jackson.DateTimeSerializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CustomObjectMapper {
    private static CustomObjectMapper customObjectMapper;
    private static ObjectMapper objectMapper;

    private CustomObjectMapper() {
        Jackson2ObjectMapperBuilderCustomizer customizer =
                jacksonObjectMapperBuilder -> {
                    jacksonObjectMapperBuilder.featuresToEnable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
                    jacksonObjectMapperBuilder.serializerByType(BigDecimal.class, new Min2DecimalPlacesBigDecimalSerializer(BigDecimal.class));
                    jacksonObjectMapperBuilder.deserializerByType(BigDecimal.class, new BigDecimalDeserializer());
                    jacksonObjectMapperBuilder.serializerByType(DateTime.class, new DateTimeSerializer(DateTime.class));
                    jacksonObjectMapperBuilder.deserializerByType(DateTime.class, new DateTimeDeserializer());
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

    public static class Min2DecimalPlacesBigDecimalSerializer extends StdSerializer<BigDecimal> {

        public Min2DecimalPlacesBigDecimalSerializer(Class<BigDecimal> t) {
            super(t);
        }

        @Override
        public void serialize(BigDecimal bigDecimal, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeString(String.valueOf(bigDecimal));
        }
    }

    public static class BigDecimalDeserializer extends NumberDeserializers.BigDecimalDeserializer {
        @Override
        public BigDecimal deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            return super.deserialize(parser, context);
        }

    }
}
