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

/*package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.validation;

import com.adelean.inject.resources.junit.jupiter.GivenTextResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import com.forgerock.securebanking.openbanking.uk.common.api.meta.share.IntentType;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType;
import com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.validation.services.FilePaymentFileValidationService;
import com.forgerock.securebanking.openbanking.uk.rs.common.filepayment.PaymentFile;
import com.forgerock.securebanking.openbanking.uk.rs.common.filepayment.PaymentFileFactory;
import com.forgerock.securebanking.openbanking.uk.rs.common.filepayment.PaymentFileType;
import com.forgerock.securebanking.openbanking.uk.rs.common.util.HashUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.org.openbanking.datamodel.payment.OBWriteFile2DataInitiation;
import uk.org.openbanking.datamodel.payment.OBWriteFileConsentResponse4;
import uk.org.openbanking.testsupport.payment.OBWriteFileConsentTestDataFactory;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@TestWithResources
public class FilePaymentFileValidationServiceTest {

    private FilePaymentFileValidationService filePaymentFileValidationService = new FilePaymentFileValidationService();

    @GivenTextResource("payment/files/UK_OBIE_pain_001_001_08.xml")
    static String UK_OBIE_pain_001_001_08_Content;

    @GivenTextResource("payment/files/UK_OBIE_PaymentInitiation_3_1.json")
    static String UK_OBIE_PaymentInitiation_3_1_Content;

    private static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                Arguments.arguments(
                        PaymentFileType.UK_OBIE_PAIN_001
                ),
                Arguments.arguments(
                        PaymentFileType.UK_OBIE_PAYMENT_INITIATION_V3_1
                )
        );
    }

    @ParameterizedTest
    @MethodSource("argumentsProvider")
    public void shouldValidate(PaymentFileType paymentFileType) throws OBErrorException {
        // Given
        String consentId = IntentType.PAYMENT_FILE_CONSENT.generateIntentId();
        String fileContent = getFileContent(paymentFileType);
        OBWriteFileConsentResponse4 consentResponse4 = OBWriteFileConsentTestDataFactory.aValidOBWriteFileConsentResponse4(consentId);
        OBWriteFile2DataInitiation initiation = consentResponse4.getData().getInitiation();
        PaymentFile paymentFile = PaymentFileFactory.createPaymentFile(paymentFileType, fileContent);
        initiation.setFileHash(HashUtils.computeSHA256FullHash(fileContent));
        initiation.setControlSum(paymentFile.getControlSum());
        initiation.setFileType(paymentFileType.getFileType());
        initiation.setNumberOfTransactions(String.valueOf(paymentFile.getNumberOfTransactions()));
        // when
        filePaymentFileValidationService.clearErrors().validate(
                fileContent,
                initiation,
                paymentFileType
        );
        // Then
        assertThat(filePaymentFileValidationService.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("argumentsProvider")
    public void shouldFailControlSum(PaymentFileType paymentFileType) throws OBErrorException {
        // Given
        String consentId = IntentType.PAYMENT_FILE_CONSENT.generateIntentId();
        String fileContent = getFileContent(paymentFileType);
        OBWriteFileConsentResponse4 consentResponse4 = OBWriteFileConsentTestDataFactory.aValidOBWriteFileConsentResponse4(consentId);
        OBWriteFile2DataInitiation initiation = consentResponse4.getData().getInitiation();
        PaymentFile paymentFile = PaymentFileFactory.createPaymentFile(paymentFileType, fileContent);
        initiation.setFileHash(HashUtils.computeSHA256FullHash(fileContent));
        initiation.setControlSum(BigDecimal.ONE);
        initiation.setFileType(paymentFileType.getFileType());
        initiation.setNumberOfTransactions(String.valueOf(paymentFile.getNumberOfTransactions()));
        // when
        filePaymentFileValidationService.clearErrors().validate(
                fileContent,
                initiation,
                paymentFileType
        );
        // Then
        assertThat(filePaymentFileValidationService.getErrors()).isNotEmpty().containsExactly(
                new OBErrorException(
                        OBRIErrorType.REQUEST_FILE_INCORRECT_CONTROL_SUM,
                        paymentFile.getControlSum().toPlainString(),
                        initiation.getControlSum().toPlainString()
                ).getOBError()
        );
    }

    @ParameterizedTest
    @MethodSource("argumentsProvider")
    public void shouldFailFileHash(PaymentFileType paymentFileType) throws OBErrorException {
        // Given
        String wrongHash = UUID.randomUUID().toString();
        String consentId = IntentType.PAYMENT_FILE_CONSENT.generateIntentId();
        String fileContent = getFileContent(paymentFileType);
        OBWriteFileConsentResponse4 consentResponse4 = OBWriteFileConsentTestDataFactory.aValidOBWriteFileConsentResponse4(consentId);
        OBWriteFile2DataInitiation initiation = consentResponse4.getData().getInitiation();
        PaymentFile paymentFile = PaymentFileFactory.createPaymentFile(paymentFileType, fileContent);
        initiation.setFileHash(wrongHash);
        initiation.setControlSum(paymentFile.getControlSum());
        initiation.setFileType(paymentFileType.getFileType());
        initiation.setNumberOfTransactions(String.valueOf(paymentFile.getNumberOfTransactions()));
        // when
        filePaymentFileValidationService.clearErrors().validate(
                fileContent,
                initiation,
                paymentFileType
        );
        // Then
        assertThat(filePaymentFileValidationService.getErrors()).isNotEmpty().containsExactly(
                new OBErrorException(
                        OBRIErrorType.REQUEST_FILE_INCORRECT_FILE_HASH,
                        HashUtils.computeSHA256FullHash(fileContent),
                        initiation.getFileHash()
                ).getOBError()
        );
    }

    @ParameterizedTest
    @MethodSource("argumentsProvider")
    public void shouldFailNumOfTransactions(PaymentFileType paymentFileType) throws OBErrorException {
        // Given
        String consentId = IntentType.PAYMENT_FILE_CONSENT.generateIntentId();
        String fileContent = getFileContent(paymentFileType);
        OBWriteFileConsentResponse4 consentResponse4 = OBWriteFileConsentTestDataFactory.aValidOBWriteFileConsentResponse4(consentId);
        OBWriteFile2DataInitiation initiation = consentResponse4.getData().getInitiation();
        PaymentFile paymentFile = PaymentFileFactory.createPaymentFile(paymentFileType, fileContent);
        initiation.setFileHash(HashUtils.computeSHA256FullHash(fileContent));
        initiation.setControlSum(paymentFile.getControlSum());
        initiation.setFileType(paymentFileType.getFileType());
        initiation.setNumberOfTransactions("99999");
        // when
        filePaymentFileValidationService.clearErrors().validate(
                fileContent,
                initiation,
                paymentFileType
        );
        // Then
        assertThat(filePaymentFileValidationService.getErrors()).isNotEmpty().containsExactly(
                new OBErrorException(
                        OBRIErrorType.REQUEST_FILE_WRONG_NUMBER_OF_TRANSACTIONS,
                        paymentFile.getNumberOfTransactions(),
                        initiation.getNumberOfTransactions()
                ).getOBError()
        );
    }

    @ParameterizedTest
    @MethodSource("argumentsProvider")
    public void shouldFailAll(PaymentFileType paymentFileType) throws OBErrorException {
        // Given
        String wrongHash = UUID.randomUUID().toString();
        String consentId = IntentType.PAYMENT_FILE_CONSENT.generateIntentId();
        String fileContent = getFileContent(paymentFileType);
        OBWriteFileConsentResponse4 consentResponse4 = OBWriteFileConsentTestDataFactory.aValidOBWriteFileConsentResponse4(consentId);
        OBWriteFile2DataInitiation initiation = consentResponse4.getData().getInitiation();
        PaymentFile paymentFile = PaymentFileFactory.createPaymentFile(paymentFileType, fileContent);
        initiation.setFileHash(wrongHash);
        initiation.setControlSum(BigDecimal.ONE);
        initiation.setFileType(paymentFileType.getFileType());
        initiation.setNumberOfTransactions("99999");
        // when
        filePaymentFileValidationService.clearErrors().validate(
                fileContent,
                initiation,
                paymentFileType
        );
        // Then
        assertThat(filePaymentFileValidationService.getErrors()).isNotEmpty().containsExactlyInAnyOrder(
                new OBErrorException(
                        OBRIErrorType.REQUEST_FILE_WRONG_NUMBER_OF_TRANSACTIONS,
                        paymentFile.getNumberOfTransactions(),
                        initiation.getNumberOfTransactions()
                ).getOBError(),
                new OBErrorException(
                        OBRIErrorType.REQUEST_FILE_INCORRECT_CONTROL_SUM,
                        paymentFile.getControlSum().toPlainString(),
                        initiation.getControlSum().toPlainString()
                ).getOBError(),
                new OBErrorException(
                        OBRIErrorType.REQUEST_FILE_INCORRECT_FILE_HASH,
                        HashUtils.computeSHA256FullHash(fileContent),
                        initiation.getFileHash()
                ).getOBError()
        );
    }

    private String getFileContent(PaymentFileType paymentFileType) {
        String content = "";
        switch (paymentFileType) {
            case UK_OBIE_PAIN_001 -> content = UK_OBIE_pain_001_001_08_Content;
            case UK_OBIE_PAYMENT_INITIATION_V3_1 -> content = UK_OBIE_PaymentInitiation_3_1_Content;
        }
        return content;
    }
}
*/
