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

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRFilePayment;

import java.math.BigDecimal;
import java.util.List;

/**
 * Represents a parsed valid payment file
 */
public class PaymentFile {

    private final int numberOfTransactions;
    private final BigDecimal controlSum;
    private final PaymentFileType fileType;
    private final List<FRFilePayment> payments;

    public PaymentFile(int numberOfTransactions, BigDecimal controlSum, PaymentFileType fileType, List<FRFilePayment> payments) {
        this.numberOfTransactions = numberOfTransactions;
        this.controlSum = controlSum;
        this.fileType = fileType;
        this.payments = payments;
    }

    /**
     * @return Number of transactions in payment file
     */
    public int getNumberOfTransactions() {
        return numberOfTransactions;
    }

    /**
     * @return The control sum (sum of all transaction amounts)
     */
    public BigDecimal getControlSum() {
        return controlSum;
    }

    /**
     * @return PaymentFileType the type of file that was parsed
     */
    public PaymentFileType getFileType() {
        return fileType;
    }

    /**
     * @return List of each transaction with essential payment details and status
     */
    public List<FRFilePayment> getPayments() {
        return payments;
    }
}
