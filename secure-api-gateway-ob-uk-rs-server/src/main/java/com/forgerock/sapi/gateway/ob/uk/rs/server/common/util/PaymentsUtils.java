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
package com.forgerock.sapi.gateway.ob.uk.rs.server.common.util;

import uk.org.openbanking.datamodel.common.OBRisk1;
import uk.org.openbanking.datamodel.payment.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Added for operations and constants regarding the payment status for the payments APIs
 */
public class PaymentsUtils {

    public static final Map<String, String> statusLinkingMap = Map.of(
            "InitiationPending", "Pending",
            "InitiationFailed", "Rejected",
            "InitiationCompleted", "Accepted",
            "Cancelled", "Cancelled"
    );

    public static OBWriteDomesticConsentResponse4 createTestDataConsentResponse4(OBWriteDomestic2 payment) {
        List<OBWriteDomesticConsentResponse4DataCharges> charges = new ArrayList<>();
        charges.add(new OBWriteDomesticConsentResponse4DataCharges());
        return new OBWriteDomesticConsentResponse4()
                .data(new OBWriteDomesticConsentResponse4Data()
                        .initiation(payment.getData().getInitiation())
                        .charges(charges))
                .risk(payment.getRisk());
    }

    public static OBWriteDomesticConsentResponse5 createTestDataConsentResponse5(OBWriteDomestic2 payment) {
        List<OBWriteDomesticConsentResponse5DataCharges> charges = new ArrayList<>();
        charges.add(new OBWriteDomesticConsentResponse5DataCharges());
        return new OBWriteDomesticConsentResponse5()
                .data(new OBWriteDomesticConsentResponse5Data()
                        .initiation(payment.getData().getInitiation())
                        .charges(charges))
                .risk(payment.getRisk());
    }

}