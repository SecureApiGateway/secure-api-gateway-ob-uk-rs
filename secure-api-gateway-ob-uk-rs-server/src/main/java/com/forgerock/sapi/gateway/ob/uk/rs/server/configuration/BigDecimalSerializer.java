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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Custom Serializer for dealing with {@link BigDecimal} objects
 */
public class BigDecimalSerializer extends StdSerializer<BigDecimal> {

    public BigDecimalSerializer(Class<BigDecimal> t) {
        super(t);
    }

    @Override
    public void serialize(BigDecimal value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        // The code below uses the toPlainString() method to build a new BigDecimal,
        // to make sure not to write the result as scientific notation (eg 1.2E+4)
        jsonGenerator.writeNumber(new BigDecimal(
                value.toPlainString()).setScale(
                        BigDecimalDefaults.DEFAULT_SCALE, BigDecimalDefaults.DEFAULT_ROUNDING_MODE
                )
        );
    }
}
