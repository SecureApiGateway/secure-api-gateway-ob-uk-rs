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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.validation.v4;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.validation.v4.FilePaymentFileContentValidator.FilePaymentFileContentValidationContext;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.PaymentFile;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.BaseOBValidator;
import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v4.payment.OBWriteFileConsent3;

import java.math.BigDecimal;

/**
 * Validator which verifies that the Payment File uploaded is valid for the Consent
 *
 * The following properties of the uploaded file are validated against the Consent:
 * - fileHash
 * - controlSum
 * - numberOfTransactions
 */
public class FilePaymentFileContentValidator extends BaseOBValidator<FilePaymentFileContentValidationContext> {

    public static class FilePaymentFileContentValidationContext {
        private final String fileHash;
        private final PaymentFile paymentFile;
        private final OBWriteFileConsent3 obFileConsent;

        public FilePaymentFileContentValidationContext(String fileHash, PaymentFile paymentFile, OBWriteFileConsent3 obFileConsent) {
            this.fileHash = fileHash;
            this.paymentFile = paymentFile;
            this.obFileConsent = obFileConsent;
        }

        public String getFileHash() {
            return fileHash;
        }

        public PaymentFile getPaymentFile() {
            return paymentFile;
        }

        public OBWriteFileConsent3 getObFileConsent() {
            return obFileConsent;
        }
    }

    @Override
    protected void validate(FilePaymentFileContentValidationContext validationContext,
                            ValidationResult<OBError1> validationResult) {

        final OBWriteFileConsent3 obFileConsent = validationContext.getObFileConsent();

        final String fileHash = validationContext.getFileHash();
        final String consentHash = obFileConsent.getData().getInitiation().getFileHash();
        if (!fileHash.equals(consentHash)) {
            validationResult.addError(OBRIErrorType.REQUEST_FILE_INCORRECT_FILE_HASH.toOBError1(fileHash, consentHash));
            // Fail fast if the hash is invalid as the file has been tampered with
            return;
        }

        final PaymentFile paymentFile = validationContext.getPaymentFile();

        final int numTransactionsInConsent = Integer.parseInt(obFileConsent.getData().getInitiation().getNumberOfTransactions());
        final int numTransactionsInFile = paymentFile.getNumberOfTransactions();
        if (numTransactionsInFile != numTransactionsInConsent) {
            validationResult.addError(OBRIErrorType.REQUEST_FILE_WRONG_NUMBER_OF_TRANSACTIONS.toOBError1(numTransactionsInFile, numTransactionsInConsent));
        }
        final BigDecimal consentControlSum = obFileConsent.getData().getInitiation().getControlSum();
        if (paymentFile.getControlSum().compareTo(consentControlSum) != 0) {
            validationResult.addError(OBRIErrorType.REQUEST_FILE_INCORRECT_CONTROL_SUM.toOBError1(paymentFile.getControlSum(), consentControlSum));
        }
    }

}
