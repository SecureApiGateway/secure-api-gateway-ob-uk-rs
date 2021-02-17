/**
 * Copyright © 2020 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.persistence.document.payment;

import java.util.stream.Stream;

/**
 * Represents the status of a Payment (not to be confused with the status of the payment's consent).
 */
public enum FRSubmissionStatus {

    // "Immediate" Domestic/International Payments
    PENDING("Pending"),
    REJECTED("Rejected"),
    ACCEPTEDSETTLEMENTINPROCESS("AcceptedSettlementInProcess"),
    ACCEPTEDCREDITSETTLEMENTCOMPLETED("AcceptedCreditSettlementCompleted"),
    ACCEPTEDWITHOUTPOSTING("AcceptedWithoutPosting"),
    ACCEPTEDSETTLEMENTCOMPLETED("AcceptedSettlementCompleted"),

    // Domestic/International Scheduled Payments or Standing Orders, or File Payments
    INITIATIONPENDING("InitiationPending"),
    INITIATIONFAILED("InitiationFailed"),
    INITIATIONCOMPLETED("InitiationCompleted"),
    CANCELLED("Cancelled");

    private String value;

    FRSubmissionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return value;
    }

    public static FRSubmissionStatus fromValue(String value) {
        return Stream.of(values())
                .filter(type -> type.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }
}
