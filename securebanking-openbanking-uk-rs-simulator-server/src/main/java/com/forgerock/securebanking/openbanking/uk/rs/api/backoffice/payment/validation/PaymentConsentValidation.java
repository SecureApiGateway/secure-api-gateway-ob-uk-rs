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
package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.validation;

import com.forgerock.securebanking.openbanking.uk.common.api.meta.obie.OBVersion;

public abstract class PaymentConsentValidation extends GenericValidations {

    /**
     *
     * @param version {@link OBVersion} is the api version to identify the request object to be validated
     * @return the request consent class by version
     */
    public abstract Class getRequestClass(OBVersion version);

    /**
     *
     * @param consent the consent request object
     * @param <T> dealing generic type
     */
    public abstract <T> void validate(T consent);

}
