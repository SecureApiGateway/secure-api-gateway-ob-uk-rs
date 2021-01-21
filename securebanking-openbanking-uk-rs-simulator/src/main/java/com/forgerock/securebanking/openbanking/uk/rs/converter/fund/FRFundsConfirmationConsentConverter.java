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
package com.forgerock.securebanking.openbanking.uk.rs.converter.fund;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.funds.FRFundsConfirmationConsentData;
import uk.org.openbanking.datamodel.fund.OBFundsConfirmationConsent1;
import com.forgerock.securebanking.openbanking.uk.rs.converter.FRAccountIdentifierConverter;

public class FRFundsConfirmationConsentConverter {

    public static FRFundsConfirmationConsentData toFRFundsConfirmationConsentData(OBFundsConfirmationConsent1 obFundsConfirmationConsent) {
        return obFundsConfirmationConsent == null ? null : FRFundsConfirmationConsentData.builder()
                .expirationDateTime(obFundsConfirmationConsent.getData().getExpirationDateTime())
                .debtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(obFundsConfirmationConsent.getData().getDebtorAccount()))
                .build();
    }
}
