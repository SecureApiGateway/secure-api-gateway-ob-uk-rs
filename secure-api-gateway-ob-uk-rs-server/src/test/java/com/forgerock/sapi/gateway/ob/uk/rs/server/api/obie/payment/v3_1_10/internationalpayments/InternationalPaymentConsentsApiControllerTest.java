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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.internationalpayments;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRExchangeRateInformation;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRExchangeRateInformation.FRRateType;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteInternationalConsentConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.international.v3_1_10.InternationalPaymentConsent;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

import uk.org.openbanking.datamodel.payment.OBWriteInternationalConsent5;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalConsentResponse6Data.StatusEnum;
import uk.org.openbanking.testsupport.payment.OBWriteInternationalConsentTestDataFactory;

public class InternationalPaymentConsentsApiControllerTest {

    private static OBWriteInternationalConsent5 createValidateConsentRequest() {
        final OBWriteInternationalConsent5 consentRequest = OBWriteInternationalConsentTestDataFactory.aValidOBWriteInternationalConsent5();
        consentRequest.getData().getAuthorisation().setCompletionDateTime(DateTime.now(DateTimeZone.UTC));
        return consentRequest;
    }

    public static InternationalPaymentConsent buildAwaitingAuthorisationConsent(OBWriteInternationalConsent5 consentRequest) {
        final InternationalPaymentConsent consentStoreResponse = new InternationalPaymentConsent();
        consentStoreResponse.setId(IntentType.PAYMENT_INTERNATIONAL_CONSENT.generateIntentId());
        consentStoreResponse.setRequestObj(FRWriteInternationalConsentConverter.toFRWriteInternationalConsent(consentRequest));
        consentStoreResponse.setStatus(StatusEnum.AWAITINGAUTHORISATION.toString());
        consentStoreResponse.setCharges(List.of());
        consentStoreResponse.setExchangeRateInformation(FRExchangeRateInformation.builder().rateType(FRRateType.AGREED).
                exchangeRate(new BigDecimal("1.1")).
                unitCurrency("GBP")
                .contractIdentification("ct2334")
                .build());
        final DateTime creationDateTime = DateTime.now();
        consentStoreResponse.setCreationDateTime(creationDateTime);
        consentStoreResponse.setStatusUpdateDateTime(creationDateTime);
        return consentStoreResponse;
    }

    private static InternationalPaymentConsent buildAuthorisedConsent(OBWriteInternationalConsent5 consentRequest, String debtorAccountId) {
        final InternationalPaymentConsent internationalPaymentConsent = buildAwaitingAuthorisationConsent(consentRequest);
        internationalPaymentConsent.setStatus(StatusEnum.AUTHORISED.toString());
        internationalPaymentConsent.setAuthorisedDebtorAccountId(debtorAccountId);
        return internationalPaymentConsent;
    }
}