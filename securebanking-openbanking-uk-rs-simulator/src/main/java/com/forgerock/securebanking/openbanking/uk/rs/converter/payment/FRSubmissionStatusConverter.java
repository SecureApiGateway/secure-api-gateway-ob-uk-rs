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
package com.forgerock.securebanking.openbanking.uk.rs.converter.payment;

import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.payment.FRSubmissionStatus;
import uk.org.openbanking.datamodel.payment.OBExternalStatus1Code;
import uk.org.openbanking.datamodel.payment.OBTransactionIndividualStatus1Code;

public class FRSubmissionStatusConverter {

    // FR to OB
    public static OBTransactionIndividualStatus1Code toOBTransactionIndividualStatus1Code(FRSubmissionStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case PENDING -> OBTransactionIndividualStatus1Code.PENDING;
            case ACCEPTEDSETTLEMENTINPROCESS -> OBTransactionIndividualStatus1Code.ACCEPTEDSETTLEMENTINPROCESS;
            case ACCEPTEDSETTLEMENTCOMPLETED,
                    ACCEPTEDCREDITSETTLEMENTCOMPLETED,
                    ACCEPTEDWITHOUTPOSTING -> OBTransactionIndividualStatus1Code.ACCEPTEDSETTLEMENTCOMPLETED;
            default -> OBTransactionIndividualStatus1Code.REJECTED;
        };
    }

    public static OBExternalStatus1Code toOBExternalStatus1Code(FRSubmissionStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case INITIATIONPENDING -> OBExternalStatus1Code.INITIATIONPENDING;
            case INITIATIONCOMPLETED -> OBExternalStatus1Code.INITIATIONCOMPLETED;
            default -> OBExternalStatus1Code.INITIATIONFAILED;
        };
    }
}
