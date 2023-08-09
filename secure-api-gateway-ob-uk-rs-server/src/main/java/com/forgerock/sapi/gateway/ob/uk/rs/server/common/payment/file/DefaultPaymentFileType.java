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

import org.springframework.http.MediaType;

/**
 * Enum containing PaymentFileType information for the default set of file types supported by the OBIE schema
 */
public enum DefaultPaymentFileType {

    UK_OBIE_PAYMENT_INITIATION_V3_1("UK.OBIE.PaymentInitiation.3.1", MediaType.APPLICATION_JSON),
    UK_OBIE_PAIN_001("UK.OBIE.pain.001.001.08", MediaType.TEXT_XML);

    private final PaymentFileType paymentFileType;

    DefaultPaymentFileType(String fileType, MediaType contentType) {
        this.paymentFileType = new PaymentFileType(fileType, contentType);
    }

    public PaymentFileType getPaymentFileType() {
        return paymentFileType;
    }

    public static DefaultPaymentFileType fromFileType(String value) {
        for (DefaultPaymentFileType defaultPaymentFileType : DefaultPaymentFileType.values()) {
            if (defaultPaymentFileType.paymentFileType.getFileType().equals(value)) {
                return defaultPaymentFileType;
            }
        }
        throw new UnsupportedOperationException("Unsupported payment file type: '" + value + "'");
    }
}
