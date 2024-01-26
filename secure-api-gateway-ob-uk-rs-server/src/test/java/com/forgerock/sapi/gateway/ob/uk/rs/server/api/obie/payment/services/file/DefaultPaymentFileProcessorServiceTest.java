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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.file;

import static com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.BasePaymentFileProcessorTest.validateProcessedPaymentFileResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.http.MediaType;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRFilePayment;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.DefaultPaymentFileType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.PaymentFile;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.PaymentFileType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.processor.xml.OBIEPain001FileProcessor;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.processor.json.OBIEPaymentInitiation31FileProcessor;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.processor.PaymentFileProcessor;
import com.forgerock.sapi.gateway.ob.uk.rs.server.util.payment.file.TestPaymentFileResources;
import com.forgerock.sapi.gateway.ob.uk.rs.server.util.payment.file.TestPaymentFileResources.TestPaymentFile;

class DefaultPaymentFileProcessorServiceTest {

    public static final List<PaymentFileProcessor> OBIE_FILE_TYPE_PROCESSORS = List.of(new OBIEPaymentInitiation31FileProcessor(),
                                                                                       new OBIEPain001FileProcessor());
    private static final PaymentFileType UNIT_TEST_TYPE = new PaymentFileType("unitTestType", MediaType.APPLICATION_JSON);

    /**
     * Example extension PaymentFileProcessor impl for unit testing purposes
     */
    private static class UnitTestPaymentFileTypeProcessor implements PaymentFileProcessor {

        private final PaymentFile fixedPaymentFileResponse;

        private UnitTestPaymentFileTypeProcessor(PaymentFile fixedPaymentFileResponse) {
            this.fixedPaymentFileResponse = fixedPaymentFileResponse;
        }

        @Override
        public PaymentFileType getSupportedFileType() {
            return UNIT_TEST_TYPE;
        }

        // Test impl doesn't do any processing, simply returns the fixedPaymentFileResponse
        @Override
        public PaymentFile processFile(String fileContent) {
            return fixedPaymentFileResponse;
        }
    }

    @Test
    void failsToConstructProcessorServiceForDuplicatePaymentFileTypes() {
        // Attempt to create service with duplicate file processor impl
        IllegalStateException illegalStateException = assertThrows(IllegalStateException.class,
                () -> new DefaultPaymentFileProcessorService(List.of(new OBIEPaymentInitiation31FileProcessor(), new OBIEPaymentInitiation31FileProcessor())));
        assertThat(illegalStateException.getMessage()).isEqualTo("Duplicate paymentFileProcessor for fileType: UK.OBIE.PaymentInitiation.3.1");

        // Create another impl of processor with the UK_OBIE_PAYMENT_INITIATION_V3_1 fileType
        final PaymentFileProcessor diffImplOfOBIEPaymentInitiationv31FileProcessor = new PaymentFileProcessor() {
            @Override
            public PaymentFileType getSupportedFileType() {
                return DefaultPaymentFileType.UK_OBIE_PAYMENT_INITIATION_V3_1.getPaymentFileType();
            }

            @Override
            public PaymentFile processFile(String fileContent) {
                return null;
            }
        };
        illegalStateException = assertThrows(IllegalStateException.class,
                () -> new DefaultPaymentFileProcessorService(List.of(diffImplOfOBIEPaymentInitiationv31FileProcessor, new OBIEPaymentInitiation31FileProcessor())));
        assertThat(illegalStateException.getMessage()).isEqualTo("Duplicate paymentFileProcessor for fileType: UK.OBIE.PaymentInitiation.3.1");
    }

    @NullAndEmptySource
    @ParameterizedTest
    void failsToConstructServiceIfProcessorListNullOrEmpty(List<PaymentFileProcessor> paymentFileProcessors) {
        final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> new DefaultPaymentFileProcessorService(paymentFileProcessors));
        assertThat(illegalArgumentException.getMessage()).isEqualTo("1 or more paymentFileProcessors must be supplied");
    }

    @Test
    void shouldSupportProcessingDefaultObieFilePaymentTypes() throws OBErrorException {
        // Create the service with the default OBIE File Payment types supported
        final DefaultPaymentFileProcessorService paymentFileProcessorService = new DefaultPaymentFileProcessorService(OBIE_FILE_TYPE_PROCESSORS);

        // Verify the findPaymentFileType lookup works
        for (PaymentFileProcessor paymentFileProcessor : OBIE_FILE_TYPE_PROCESSORS) {
            final PaymentFileType supportedFileType = paymentFileProcessor.getSupportedFileType();
            assertThat(paymentFileProcessorService.findPaymentFileType(supportedFileType.getFileType())).isEqualTo(paymentFileProcessor.getSupportedFileType());
        }

        // Verify that we can process all TestPaymentFiles
        final TestPaymentFileResources testPaymentFileResources = TestPaymentFileResources.getInstance();
        for (final TestPaymentFile testPaymentFile : testPaymentFileResources.getPaymentFiles().values()) {
            final String obieFileType = testPaymentFile.getFileType().getFileType();
            final String fileContent = testPaymentFile.getFileContent();
            validateProcessedPaymentFileResult(testPaymentFile, paymentFileProcessorService.processFile(obieFileType, fileContent));
        }
    }

    @Test
    void shouldSupportProcessingCustomFilePaymentTypes() throws OBErrorException {
        final List<PaymentFileProcessor> fileTypeProcessors = new ArrayList<>(OBIE_FILE_TYPE_PROCESSORS);
        // Configure the UnitTestPaymentFileType processor to return some pre-canned data
        final PaymentFile processorPaymentFileResponse = new PaymentFile(UNIT_TEST_TYPE, List.of(new FRFilePayment()), new BigDecimal("343434.34"));
        fileTypeProcessors.add(new UnitTestPaymentFileTypeProcessor(processorPaymentFileResponse));

        final DefaultPaymentFileProcessorService paymentFileProcessorService = new DefaultPaymentFileProcessorService(fileTypeProcessors);
        assertThat(paymentFileProcessorService.findPaymentFileType(UNIT_TEST_TYPE.getFileType())).isEqualTo(UNIT_TEST_TYPE);
        assertThat(paymentFileProcessorService.processFile(UNIT_TEST_TYPE.getFileType(), "example file")).isEqualTo(processorPaymentFileResponse);
    }

    @Test
    void failsToFindPaymentFileTypeForUnknownType() {
        final OBErrorException obErrorException = assertThrows(OBErrorException.class,
                () -> new DefaultPaymentFileProcessorService(OBIE_FILE_TYPE_PROCESSORS).findPaymentFileType("weird format"));
        assertThat(obErrorException.getOBError().getErrorCode()).isEqualTo("OBRI.Request.File.Payment.FileType.Not.Supported");
        assertThat(obErrorException.getMessage()).contains("The Payment FileType: 'weird format' is not supported");
    }

    @Test
    void failsToProcessFileForUnknownType() {
        final OBErrorException obErrorException = assertThrows(OBErrorException.class,
                () -> new DefaultPaymentFileProcessorService(OBIE_FILE_TYPE_PROCESSORS).processFile("weird format", "fgdfgdgdgdfg"));
        assertThat(obErrorException.getOBError().getErrorCode()).isEqualTo("OBRI.Request.File.Payment.FileType.Not.Supported");
        assertThat(obErrorException.getMessage()).contains("The Payment FileType: 'weird format' is not supported");
    }

    @Test
    void failsWhenFileProcessorRaisesOBException() {
        final OBErrorException obErrorException = assertThrows(OBErrorException.class,
                () -> new DefaultPaymentFileProcessorService(OBIE_FILE_TYPE_PROCESSORS).processFile(
                        DefaultPaymentFileType.UK_OBIE_PAIN_001.getPaymentFileType().getFileType(), "junk"));
        assertThat(obErrorException.getOBError().getErrorCode()).isEqualTo("OBRI.Request.Object.file.invalid");
    }

    @Test
    void failsWhenFileProcessorRaisesUnexpectedException() {
        final DefaultPaymentFileProcessorService paymentFileProcessorService = new DefaultPaymentFileProcessorService(List.of(new PaymentFileProcessor(){
            @Override
            public PaymentFileType getSupportedFileType() {
                return UNIT_TEST_TYPE;
            }

            @Override
            public PaymentFile processFile(String fileContent) {
                throw new NullPointerException("boom");
            }
        }));
        final OBErrorException obErrorException = assertThrows(OBErrorException.class,
                () -> paymentFileProcessorService.processFile(
                        UNIT_TEST_TYPE.getFileType(), "junk"));
        assertThat(obErrorException.getOBError().getErrorCode()).isEqualTo("OBRI.Request.Object.file.invalid");
    }

}