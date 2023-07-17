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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.validation;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.validation.FilePaymentFileContentValidator.FilePaymentFileContentValidationContext;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.filepayment.PaymentFile;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.filepayment.PaymentFileFactory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.filepayment.PaymentFileType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.HashUtils;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.BaseOBValidator;

import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.payment.OBWriteFileConsent3;

/**
 * Validator which verifies that the Content of the Payment File uploaded is valid.
 *
 * Checks applied:
 * - Parses the file as the fileType defined in the consent
 * - Validates the fileHash
 */
public class FilePaymentFileContentValidator extends BaseOBValidator<FilePaymentFileContentValidationContext> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static class FilePaymentFileContentValidationContext {
        private final String fileContent;
        private final OBWriteFileConsent3 obFileConsent;

        public FilePaymentFileContentValidationContext(String fileContent, OBWriteFileConsent3 obFileConsent) {
            this.fileContent = fileContent;
            this.obFileConsent = obFileConsent;
        }

        public String getFileContent() {
            return fileContent;
        }

        public OBWriteFileConsent3 getObFileConsent() {
            return obFileConsent;
        }
    }

    @Override
    protected void validate(FilePaymentFileContentValidationContext filePaymentFileContentValidationContext,
                            ValidationResult<OBError1> validationResult) {

        final String fileContent = filePaymentFileContentValidationContext.getFileContent();
        final OBWriteFileConsent3 obFileConsent = filePaymentFileContentValidationContext.getObFileConsent();

        final String fileHash = HashUtils.computeSHA256FullHash(fileContent);
        final String consentHash = obFileConsent.getData().getInitiation().getFileHash();
        if (!fileHash.equals(consentHash)) {
            validationResult.addError(OBRIErrorType.REQUEST_FILE_INCORRECT_FILE_HASH.toOBError1(fileHash, consentHash));
            return;
        }

        PaymentFile paymentFile;
        try {
            paymentFile = PaymentFileFactory.createPaymentFile(PaymentFileType.fromFileType(obFileConsent.getData().getInitiation().getFileType()), fileContent);

            final String numTransactionsInConsent = obFileConsent.getData().getInitiation().getNumberOfTransactions();
            final String numTransactionsInFile = String.valueOf(paymentFile.getNumberOfTransactions());
            if (!numTransactionsInFile.equals(numTransactionsInConsent)) {
                validationResult.addError(OBRIErrorType.REQUEST_FILE_WRONG_NUMBER_OF_TRANSACTIONS.toOBError1(numTransactionsInFile, numTransactionsInConsent));
            }
            final BigDecimal consentControlSum = obFileConsent.getData().getInitiation().getControlSum();
            if (paymentFile.getControlSum().compareTo(consentControlSum) != 0) {
                validationResult.addError(OBRIErrorType.REQUEST_FILE_INCORRECT_CONTROL_SUM.toOBError1(paymentFile.getControlSum(), consentControlSum));
            }

        } catch (OBErrorException e) {
           logger.warn("Failed to parse payment file", e);
           validationResult.addError(e.getOBError());
        }

    }

}
