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
package com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.processor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRFilePayment;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.PaymentFile;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.PaymentFileType;

/**
 * Abstract class that can be used as a base for PaymentFileProcessor implementations.
 *
 * Subclasses need to implement the processFileImpl method.
 */
public abstract class BasePaymentFileProcessor implements PaymentFileProcessor {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final PaymentFileType supportedPaymentFileType;

    protected BasePaymentFileProcessor(PaymentFileType supportedPaymentFileType) {
        this.supportedPaymentFileType = Objects.requireNonNull(supportedPaymentFileType,
                "supportedPaymentFileType must be supplied");
    }

    /**
     * FileType specific processing implementation
     */
    protected abstract PaymentFile processFileImpl(String fileContent) throws OBErrorException;

    @Override
    public PaymentFileType getSupportedFileType() {
        return supportedPaymentFileType;
    }

    @Override
    public PaymentFile processFile(String fileContent) throws OBErrorException {
        if (fileContent == null || fileContent.isEmpty()) {
            throw new OBErrorException(OBRIErrorType.REQUEST_FILE_EMPTY);
        }
        logger.debug("Parsing file content: {}", fileContent);
        final PaymentFile paymentFile = processFileImpl(fileContent);
        logger.debug("Parsed payment file, numTransactions: {}, controlSum: {}",
                     paymentFile.getNumberOfTransactions(), paymentFile.getControlSum());
        return paymentFile;
    }

    protected PaymentFile createPaymentFile(List<FRFilePayment> payments, BigDecimal controlSum) {
        return new PaymentFile(supportedPaymentFileType, payments, controlSum);
    }
}
