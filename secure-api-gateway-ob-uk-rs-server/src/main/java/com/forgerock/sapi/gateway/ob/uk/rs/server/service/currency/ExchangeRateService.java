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
package com.forgerock.sapi.gateway.ob.uk.rs.server.service.currency;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRExchangeRateInformation;

import uk.org.openbanking.datamodel.payment.OBWriteInternational3DataInitiationExchangeRateInformation;

/**
 * Calculates Exchange Rates for Consent Responses
 */
public interface ExchangeRateService {

    /**
     * Calculates Exchange Rate Information to use in a Consent Response
     *
     * @param currencyOfTransfer             String currencyCode that the payment is being made in
     * @param instructedAmount               FRAmount payment amount to be taken from the DebtorAccount
     * @param consentRequestExchangeRateInfo OBWriteInternational3DataInitiationExchangeRateInformation, the
     *                                       exchange rate information from the Consent Request. This is an optional field
     *
     * @return FRExchangeRateInformation populated with the exchange rate information to be returned in a Consent Response,
     * for indicative and actual quotes this will include an exchangeRate value computed by this service.
     */
    FRExchangeRateInformation calculateExchangeRateInfo(String currencyOfTransfer, FRAmount instructedAmount,
            OBWriteInternational3DataInitiationExchangeRateInformation consentRequestExchangeRateInfo);

}
