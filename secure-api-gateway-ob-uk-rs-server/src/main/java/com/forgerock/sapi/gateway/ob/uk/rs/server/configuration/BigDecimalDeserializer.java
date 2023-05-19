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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Custom deserializer that contains functionality for dealing with {@link BigDecimal} values.
 */
public class BigDecimalDeserializer extends StdDeserializer<BigDecimal> {
    public BigDecimalDeserializer(Class<?> vc) {
        super(vc);
    }

    public BigDecimalDeserializer() {
        this(null);
    }

    @Override
    public BigDecimal deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        // The code below uses the toPlainString() method to build a new BigDecimal,
        // to avoid write the result in scientific notation (ex. 1.2E+4)
        BigDecimal newBigDecimal = new BigDecimal(jsonParser.getDecimalValue().toPlainString()).setScale(
                BigDecimalDefaults.DEFAULT_SCALE, BigDecimalDefaults.DEFAULT_ROUNDING_MODE
        );
        return newBigDecimal;
    }
}
