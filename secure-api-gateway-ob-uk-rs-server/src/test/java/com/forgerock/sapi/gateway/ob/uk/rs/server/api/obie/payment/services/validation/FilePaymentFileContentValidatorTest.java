/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.validation;

import static com.forgerock.sapi.gateway.ob.uk.rs.server.util.payment.file.TestPaymentFileResources.PAIN_001_001_08_FILE_PATH;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.util.payment.file.TestPaymentFileResources.PAYMENT_INITIATION_3_1_FILE_PATH;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRFilePayment;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.validation.FilePaymentFileContentValidator.FilePaymentFileContentValidationContext;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.PaymentFile;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.HashUtils;
import com.forgerock.sapi.gateway.ob.uk.rs.server.util.payment.file.TestPaymentFileResources;
import com.forgerock.sapi.gateway.ob.uk.rs.server.util.payment.file.TestPaymentFileResources.TestPaymentFile;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;

import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v3.payment.OBWriteFileConsent3;
import uk.org.openbanking.testsupport.v3.payment.OBWriteFileConsentTestDataFactory;

class FilePaymentFileContentValidatorTest {

    private final TestPaymentFileResources testPaymentFileResources = TestPaymentFileResources.getInstance();

    private final FilePaymentFileContentValidator validator = new FilePaymentFileContentValidator();


    private static PaymentFile createPaymentFile(TestPaymentFile testPaymentFile) {
        // Payments are not validated, supply a dummy value in order to create the PaymentFile object
        final List<FRFilePayment> payments = Collections.nCopies(testPaymentFile.getNumTransactions(), new FRFilePayment());
        return new PaymentFile(testPaymentFile.getFileType(), payments, testPaymentFile.getControlSum());
    }

    @ParameterizedTest
    @ValueSource(strings = {PAYMENT_INITIATION_3_1_FILE_PATH, PAIN_001_001_08_FILE_PATH})
    void fileContentValidationSucceeds(String filePath) {
        final TestPaymentFile testPaymentFile = testPaymentFileResources.getPaymentFile(filePath);
        final OBWriteFileConsent3 obFileConsent = createConsent(testPaymentFile);

        final String fileHash = computeFileHash(testPaymentFile);
        final ValidationResult<OBError1> validationResult = validator.validate(
                new FilePaymentFileContentValidationContext(fileHash, createPaymentFile(testPaymentFile), obFileConsent));
        assertThat(validationResult.isValid()).isTrue();
    }


    @Test
    void validationFailsDueToFileHashMismatch() {
        final TestPaymentFile testPaymentFile = testPaymentFileResources.getPaymentFile(PAIN_001_001_08_FILE_PATH);
        final OBWriteFileConsent3 obFileConsent = OBWriteFileConsentTestDataFactory.aValidOBWriteFileConsent3(
                testPaymentFile.getFileType().getFileType(), "consentHashValue",
                String.valueOf(testPaymentFile.getNumTransactions()), testPaymentFile.getControlSum());

        final ValidationResult<OBError1> validationResult = validator.validate(
                new FilePaymentFileContentValidationContext("different hash value", createPaymentFile(testPaymentFile), obFileConsent));

        assertThat(validationResult.isValid()).isFalse();
        final OBError1 obError1 = validationResult.getErrors().get(0);
        assertThat(obError1.getErrorCode()).isEqualTo("OBRI.Request.Object.file.hash.no.matching.metadata");
        assertThat(obError1.getMessage()).contains("but the file consent metadata indicated that we are expecting a file hash of");
    }

    @Test
    void validationFailsDueToNumberOfTransactionsMismatch() {
        final TestPaymentFile testPaymentFile = testPaymentFileResources.getPaymentFile(PAIN_001_001_08_FILE_PATH);

        final OBWriteFileConsent3 consent = OBWriteFileConsentTestDataFactory.aValidOBWriteFileConsent3(
                testPaymentFile.getFileType().getFileType(), testPaymentFile.getFileHash(),
                "999", testPaymentFile.getControlSum());

        final ValidationResult<OBError1> validationResult = validator.validate(new FilePaymentFileContentValidationContext(
                computeFileHash(testPaymentFile), createPaymentFile(testPaymentFile), consent));

        assertThat(validationResult.isValid()).isFalse();
        final OBError1 obError1 = validationResult.getErrors().get(0);
        assertThat(obError1.getErrorCode()).isEqualTo("OBRI.Request.Object.file.wrong.number.of.transactions");
        assertThat(obError1.getMessage()).isEqualTo("The file received contains 3 transactions but the file consent metadata indicated that we are expecting a file with 999 transactions'");
    }


    @Test
    void validationFailsDueToControlSumMismatch() {
        final TestPaymentFile testPaymentFile = testPaymentFileResources.getPaymentFile(PAIN_001_001_08_FILE_PATH);

        final OBWriteFileConsent3 consent = OBWriteFileConsentTestDataFactory.aValidOBWriteFileConsent3(
                testPaymentFile.getFileType().getFileType(), testPaymentFile.getFileHash(),
                String.valueOf(testPaymentFile.getNumTransactions()), testPaymentFile.getControlSum().multiply(BigDecimal.valueOf(2)));

        final ValidationResult<OBError1> validationResult = validator.validate(new FilePaymentFileContentValidationContext(
                computeFileHash(testPaymentFile), createPaymentFile(testPaymentFile), consent));

        assertThat(validationResult.isValid()).isFalse();
        final OBError1 obError1 = validationResult.getErrors().get(0);
        assertThat(obError1.getErrorCode()).isEqualTo("OBRI.Request.Object.file.wrong.control.sum");
        assertThat(obError1.getMessage()).isEqualTo("The file received contains total transaction value of: 11500000 but the file consent metadata indicated a control sum value of 23000000.0000'");
    }

    private static String computeFileHash(TestPaymentFile testPaymentFile) {
        return HashUtils.computeSHA256FullHash(testPaymentFile.getFileContent());
    }

    private static OBWriteFileConsent3 createConsent(TestPaymentFile paymentFile) {
        return OBWriteFileConsentTestDataFactory.aValidOBWriteFileConsent3(
                paymentFile.getFileType().getFileType(), paymentFile.getFileHash(),
                String.valueOf(paymentFile.getNumTransactions()), paymentFile.getControlSum());
    }

}