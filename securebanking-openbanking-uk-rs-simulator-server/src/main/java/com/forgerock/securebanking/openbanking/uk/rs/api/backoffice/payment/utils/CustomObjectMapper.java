/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
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
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.joda.time.format.ISODateTimeFormat.dateTimeNoMillis;

public class CustomObjectMapper {
    private static CustomObjectMapper customObjectMapper;
    private static ObjectMapper objectMapper;

    private CustomObjectMapper() {
        Jackson2ObjectMapperBuilderCustomizer customizer =
                jacksonObjectMapperBuilder -> {
                    jacksonObjectMapperBuilder.featuresToEnable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
                    jacksonObjectMapperBuilder.serializerByType(BigDecimal.class, new BigDecimalSerializer(BigDecimal.class));
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

    public static class BigDecimalSerializer extends StdSerializer<BigDecimal> {

        public BigDecimalSerializer(Class<BigDecimal> t) {
            super(t);
        }

        @Override
        public void serialize(BigDecimal bigDecimal, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeString(setScale(bigDecimal));
        }

        private String setScale(BigDecimal bigDecimal) {
            if (bigDecimal.scale() > 2) {
                return bigDecimal.setScale(2, RoundingMode.HALF_EVEN).toString();
            }
            return bigDecimal.toString();
        }
    }

    public static class BigDecimalDeserializer extends NumberDeserializers.BigDecimalDeserializer {

        @Override
        public BigDecimal deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            return setScale(super.deserialize(parser, context));
        }

        private BigDecimal setScale(BigDecimal bigDecimal) {
            if (bigDecimal.scale() > 2) {
                return bigDecimal.setScale(2, RoundingMode.HALF_EVEN);
            }
            return bigDecimal;
        }
    }

    public static class DateTimeSerializer extends StdSerializer<DateTime> {

        public DateTimeSerializer(Class<DateTime> t) {
            super(t);
        }

        @Override
        public void serialize(DateTime dateTime, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeString(dateTime.toDateTimeISO().toString(dateTimeNoMillis()));
        }
    }

    public static class DateTimeDeserializer extends StdDeserializer<DateTime> {

        public DateTimeDeserializer() {
            super(DateTime.class);
        }

        @Override
        public DateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            String date = parser.getValueAsString();
            return DateTime.parse(date, dateTimeNoMillis());
        }

    }
}
