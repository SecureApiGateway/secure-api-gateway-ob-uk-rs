/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.validation;

import com.forgerock.securebanking.openbanking.uk.common.api.meta.obie.OBVersion;
import uk.org.openbanking.datamodel.payment.OBWriteFileConsent3;

/**
 * Validation class for Domestic Payment consent request
 * <li>
 *     File Payment
 *     <ul>
 *         <li>From 3.1.2 to 3.1.10 {@link OBWriteFileConsent3}</li>
 *     </ul>
 * </li>
 */
public class FilePaymentConsentValidation extends PaymentConsentValidation {
    @Override
    public Class getRequestClass(OBVersion version) {
        return OBWriteFileConsent3.class;
    }

    @Override
    public <T> void validate(T consent) {
        errors.clear();
        validateNumberTransactions(((OBWriteFileConsent3) consent).getData().getInitiation().getNumberOfTransactions());
        validateControlSum(((OBWriteFileConsent3) consent).getData().getInitiation().getControlSum());
    }
}
