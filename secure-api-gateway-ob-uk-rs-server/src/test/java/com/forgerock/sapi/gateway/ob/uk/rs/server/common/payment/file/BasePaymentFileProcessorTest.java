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
package com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.processor.PaymentFileProcessor;
import com.forgerock.sapi.gateway.ob.uk.rs.server.util.payment.file.TestPaymentFileResources;
import com.forgerock.sapi.gateway.ob.uk.rs.server.util.payment.file.TestPaymentFileResources.TestPaymentFile;

abstract class BasePaymentFileProcessorTest {

    private final TestPaymentFileResources testPaymentFileResources = TestPaymentFileResources.getInstance();

    private final PaymentFileProcessor paymentFileProcessor = createFileProcessor();

    protected abstract PaymentFileProcessor createFileProcessor();

    @Test
    void shouldParseValidFile() throws OBErrorException {
        final List<TestPaymentFile> files = testPaymentFileResources.getPaymentFiles().values().stream()
                .filter(testPaymentFile -> testPaymentFile.getFileType().equals(paymentFileProcessor.getSupportedFileType()))
                .collect(Collectors.toList());

        for (final TestPaymentFile testPaymentFile : files) {
            final PaymentFile paymentFile = paymentFileProcessor.processFile(testPaymentFile.getFileContent());
            assertThat(paymentFile).isNotNull();
            assertThat(paymentFile.getControlSum()).isEqualByComparingTo(testPaymentFile.getControlSum());
            assertThat(paymentFile.getNumberOfTransactions()).isEqualTo(testPaymentFile.getNumTransactions());
            assertThat(paymentFile.getFileType()).isEqualTo(testPaymentFile.getFileType());
        }

    }

    @Test
    void shouldThrowOBErrorForInvalidFile() {
        final Throwable throwable = catchThrowable(() -> paymentFileProcessor.processFile("junk"));
        assertThat(throwable).isInstanceOf(OBErrorException.class);

        final OBErrorException obErrorException = (OBErrorException) throwable;
        assertThat(obErrorException.getOBError().getErrorCode()).isEqualTo("OBRI.Request.Object.file.invalid");
        assertThat(obErrorException.getOBError().getMessage()).startsWith("The Payment file uploaded is invalid");
    }

}