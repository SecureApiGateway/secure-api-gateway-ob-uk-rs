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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDataDomesticScheduled;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDomesticScheduled;
import uk.org.openbanking.datamodel.payment.OBWriteDataDomesticScheduled1;
import uk.org.openbanking.datamodel.payment.OBWriteDataDomesticScheduled2;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduled1;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduled2;

public class FRWriteDomesticScheduledConverter {

    //OB to FR
    public static FRWriteDomesticScheduled toFRWriteDomesticScheduled(OBWriteDomesticScheduled1 obWriteDomesticScheduled1) {
        return obWriteDomesticScheduled1 == null ? null : FRWriteDomesticScheduled.builder()
                .data(toFRWriteDataDomesticScheduled(obWriteDomesticScheduled1.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(obWriteDomesticScheduled1.getRisk()))
                .build();
    }

    public static FRWriteDomesticScheduled toFRWriteDomesticScheduled(OBWriteDomesticScheduled2 obWriteDomesticScheduled2) {
        return obWriteDomesticScheduled2 == null ? null : FRWriteDomesticScheduled.builder()
                .data(toFRWriteDataDomesticScheduled(obWriteDomesticScheduled2.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(obWriteDomesticScheduled2.getRisk()))
                .build();
    }

    public static FRWriteDataDomesticScheduled toFRWriteDataDomesticScheduled(OBWriteDataDomesticScheduled1 data) {
        return data == null ? null : FRWriteDataDomesticScheduled.builder()
                .consentId(data.getConsentId())
                .initiation(FRWriteDomesticScheduledConsentConverter.toFRWriteDomesticScheduledDataInitiation(data.getInitiation()))
                .build();
    }

    public static FRWriteDataDomesticScheduled toFRWriteDataDomesticScheduled(OBWriteDataDomesticScheduled2 data) {
        return data == null ? null : FRWriteDataDomesticScheduled.builder()
                .consentId(data.getConsentId())
                .initiation(FRWriteDomesticScheduledConsentConverter.toFRWriteDomesticScheduledDataInitiation(data.getInitiation()))
                .build();
    }

    // FR to OB
    public static OBWriteDomesticScheduled2 toOBWriteDomesticScheduled2(FRWriteDomesticScheduled domesticScheduledPayment) {
        return domesticScheduledPayment == null ? null : new OBWriteDomesticScheduled2()
                .data(toOBWriteDataDomesticScheduled2(domesticScheduledPayment.getData()))
                .risk(FRPaymentRiskConverter.toOBRisk1(domesticScheduledPayment.getRisk()));
    }

    public static OBWriteDataDomesticScheduled2 toOBWriteDataDomesticScheduled2(FRWriteDataDomesticScheduled data) {
        return data == null ? null : new OBWriteDataDomesticScheduled2()
                .consentId(data.getConsentId())
                .initiation(FRWriteDomesticScheduledConsentConverter.toOBDomesticScheduled2(data.getInitiation()));
    }
}
