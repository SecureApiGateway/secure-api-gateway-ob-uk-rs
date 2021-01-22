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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalScheduled;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalScheduledData;
import uk.org.openbanking.datamodel.payment.*;

public class FRWriteInternationalScheduledConverter {

    // OB to FR
    public static FRWriteInternationalScheduled toFRWriteInternationalScheduled(OBWriteInternationalScheduled1 internationalScheduledPayment) {
        return internationalScheduledPayment == null ? null : FRWriteInternationalScheduled.builder()
                .data(toFRWriteInternationalScheduledData(internationalScheduledPayment.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(internationalScheduledPayment.getRisk()))
                .build();
    }

    public static FRWriteInternationalScheduled toFRWriteInternationalScheduled(OBWriteInternationalScheduled2 internationalScheduledPayment) {
        return internationalScheduledPayment == null ? null : FRWriteInternationalScheduled.builder()
                .data(toFRWriteInternationalScheduledData(internationalScheduledPayment.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(internationalScheduledPayment.getRisk()))
                .build();
    }

    public static FRWriteInternationalScheduled toFRWriteInternationalScheduled(OBWriteInternationalScheduled3 internationalScheduledPayment) {
        return internationalScheduledPayment == null ? null : FRWriteInternationalScheduled.builder()
                .data(toFRWriteInternationalScheduledData(internationalScheduledPayment.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(internationalScheduledPayment.getRisk()))
                .build();
    }

    public static FRWriteInternationalScheduledData toFRWriteInternationalScheduledData(OBWriteDataInternationalScheduled1 data) {
        return data == null ? null : FRWriteInternationalScheduledData.builder()
                .consentId(data.getConsentId())
                .initiation(FRWriteInternationalScheduledConsentConverter.toFRWriteInternationalScheduledDataInitiation(data.getInitiation()))
                .build();
    }

    public static FRWriteInternationalScheduledData toFRWriteInternationalScheduledData(OBWriteDataInternationalScheduled2 data) {
        return data == null ? null : FRWriteInternationalScheduledData.builder()
                .consentId(data.getConsentId())
                .initiation(FRWriteInternationalScheduledConsentConverter.toFRWriteInternationalScheduledDataInitiation(data.getInitiation()))
                .build();
    }

    public static FRWriteInternationalScheduledData toFRWriteInternationalScheduledData(OBWriteInternationalScheduled3Data data) {
        return data == null ? null : FRWriteInternationalScheduledData.builder()
                .consentId(data.getConsentId())
                .initiation(FRWriteInternationalScheduledConsentConverter.toFRWriteInternationalScheduledDataInitiation(data.getInitiation()))
                .build();
    }

    // FR to OB
    public static OBWriteInternationalScheduled2 toOBWriteInternationalScheduled2(FRWriteInternationalScheduled internationalScheduledPayment) {
        return internationalScheduledPayment == null ? null : new OBWriteInternationalScheduled2()
                .data(toOBWriteDataInternationalScheduled2(internationalScheduledPayment.getData()))
                .risk(FRPaymentRiskConverter.toOBRisk1(internationalScheduledPayment.getRisk()));
    }

    public static OBWriteDataInternationalScheduled2 toOBWriteDataInternationalScheduled2(FRWriteInternationalScheduledData data) {
        return data == null ? null : new OBWriteDataInternationalScheduled2()
                .consentId(data.getConsentId())
                .initiation(FRWriteInternationalScheduledConsentConverter.toOBInternationalScheduled2(data.getInitiation()));
    }
}
