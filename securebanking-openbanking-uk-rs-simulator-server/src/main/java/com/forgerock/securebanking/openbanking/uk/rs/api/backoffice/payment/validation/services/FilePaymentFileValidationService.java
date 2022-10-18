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
package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.validation.services;

import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType;
import com.forgerock.securebanking.openbanking.uk.rs.common.filepayment.PaymentFile;
import com.forgerock.securebanking.openbanking.uk.rs.common.filepayment.PaymentFileFactory;
import com.forgerock.securebanking.openbanking.uk.rs.common.filepayment.PaymentFileType;
import com.forgerock.securebanking.openbanking.uk.rs.common.util.HashUtils;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.payment.OBWriteFile2DataInitiation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class FilePaymentFileValidationService {

    protected List<OBError1> errors = new ArrayList<>();

    public FilePaymentFileValidationService clearErrors() {
        this.errors.clear();
        return this;
    }

    public List<OBError1> getErrors() {
        return errors;
    }

    /**
     *
     * @param fileContent content of the file to be validated
     * @param fileDataInitiation OB data initiation object to validate the file content against it
     * @return true if validation passed, false otherwise
     * @param <T> parametrized type for an instance of OB data initiation object
     */
    public <T> boolean validate(String fileContent, T fileDataInitiation, PaymentFileType paymentFileType) {
        Preconditions.checkNotNull(fileDataInitiation, "Object initiation cannot be null");
        Preconditions.checkNotNull(fileContent, "File content cannot be null");
        Preconditions.checkNotNull(paymentFileType, "File type cannot be null");
        log.debug("Validating file content {}\n against {}", fileContent, fileDataInitiation);
        try {
            PaymentFile paymentFile = PaymentFileFactory.createPaymentFile(paymentFileType, fileContent);
            if (fileDataInitiation instanceof OBWriteFile2DataInitiation) {
                OBWriteFile2DataInitiation initiation = (OBWriteFile2DataInitiation) fileDataInitiation;
                validateFileHash(HashUtils.computeSHA256FullHash(fileContent), initiation.getFileHash());
                validateNumOfTransactions(
                        paymentFile.getNumberOfTransactions(),
                        Integer.valueOf(initiation.getNumberOfTransactions())
                );
                validateControlSum(paymentFile.getControlSum(), initiation.getControlSum());
            }
        } catch (OBErrorException e) {
            errors.add(e.getOBError());
        }

        return true;
    }

    private void validateFileHash(String actualFileHash, String expectedFileHash) {
        if (!expectedFileHash.equals(actualFileHash)) {
            log.error("Expected file hash from consent metadata: '{}' " +
                    "does not match actual hash of payment file contents: '{}'", expectedFileHash, actualFileHash
            );
            errors.add(
                    new OBErrorException(
                            OBRIErrorType.REQUEST_FILE_INCORRECT_FILE_HASH, actualFileHash, expectedFileHash
                    ).getOBError()
            );
        }
    }

    private void validateNumOfTransactions(
            int paymentFileNumberOfTransactions,
            int consentNumberOfTransactions
    ) {
        log.debug(
                "Metadata indicates expected transaction count of '{}'. File contains '{}' transactions",
                consentNumberOfTransactions,
                paymentFileNumberOfTransactions
        );
        if (paymentFileNumberOfTransactions != consentNumberOfTransactions) {
            log.error(
                    "File consent metadata indicated {} transactions would be present but found {} in uploaded file",
                    consentNumberOfTransactions,
                    paymentFileNumberOfTransactions
            );
            errors.add(
                    new OBErrorException(
                            OBRIErrorType.REQUEST_FILE_WRONG_NUMBER_OF_TRANSACTIONS,
                            paymentFileNumberOfTransactions,
                            consentNumberOfTransactions
                    ).getOBError()
            );
        }
    }

    private void validateControlSum(BigDecimal fileControlSum, BigDecimal consentControlSum) {
        log.debug(
                "Metadata indicates expected control sum of '{}'. File contains actual control sum of '{}'",
                consentControlSum,
                fileControlSum
        );
        if (fileControlSum.compareTo(consentControlSum) != 0) {
            log.error(
                    "File consent metadata indicated control sum of '{}' but found a control sum of '{}' in uploaded file",
                    consentControlSum,
                    fileControlSum
            );
            errors.add(
                    new OBErrorException(
                            OBRIErrorType.REQUEST_FILE_INCORRECT_CONTROL_SUM,
                            fileControlSum.toPlainString(),
                            consentControlSum.toPlainString()
                    ).getOBError()
            );
        }
    }
}
