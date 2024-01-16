/*
 * Copyright © 2020-2024 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories;


import java.util.function.Function;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import uk.org.openbanking.datamodel.common.Links;
import uk.org.openbanking.datamodel.common.Meta;
import uk.org.openbanking.datamodel.payment.OBWriteFundsConfirmationResponse1;
import uk.org.openbanking.datamodel.payment.OBWriteFundsConfirmationResponse1Data;
import uk.org.openbanking.datamodel.payment.OBWriteFundsConfirmationResponse1DataFundsAvailableResult;

/**
 * Factory capable of producing {@link OBWriteFundsConfirmationResponse1} objects.
 * These schema objects are reused by the funds confirmation endpoint of many of the Payment APIs.
 */
@Component
public class OBWriteFundsConfirmationResponse1Factory {

    public OBWriteFundsConfirmationResponse1 create(boolean fundsAvailable, String consentId, Function<String, Links> fundsConfirmationLinksFactory) {
        return new OBWriteFundsConfirmationResponse1()
                .data(new OBWriteFundsConfirmationResponse1Data()
                        .fundsAvailableResult(new OBWriteFundsConfirmationResponse1DataFundsAvailableResult()
                                                        .fundsAvailable(fundsAvailable)
                                                        .fundsAvailableDateTime(DateTime.now())))
                .links(fundsConfirmationLinksFactory.apply(consentId))
                .meta(new Meta());
    }

}
