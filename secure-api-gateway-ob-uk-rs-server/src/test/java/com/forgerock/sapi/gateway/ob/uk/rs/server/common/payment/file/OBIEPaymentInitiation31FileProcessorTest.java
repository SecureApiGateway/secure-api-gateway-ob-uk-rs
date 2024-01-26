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
package com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.processor.json.OBIEPaymentInitiation31FileProcessor;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.processor.PaymentFileProcessor;

class OBIEPaymentInitiation31FileProcessorTest extends BasePaymentFileProcessorTest {

    @Override
    protected PaymentFileProcessor createFileProcessor() {
        return new OBIEPaymentInitiation31FileProcessor();
    }

    @Test
    void failsWhenDomesticPaymentJsonObjectIsInvalid() {
        final Throwable throwable = catchThrowable(() -> createFileProcessor().processFile("{\n" +
                "  \"Data\": {\n" +
                "    \"DomesticPayments\": [\n" +
                "      {\n" +
                "       \"invalidKey\": \"invalidValue\"" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}\n"));
        assertThat(throwable).isInstanceOf(OBErrorException.class);

        final OBErrorException obErrorException = (OBErrorException) throwable;
        assertThat(obErrorException.getOBError().getErrorCode()).isEqualTo("OBRI.Request.Object.file.invalid");
        assertThat(obErrorException.getOBError().getMessage()).startsWith("The Payment file uploaded is invalid");
    }
}