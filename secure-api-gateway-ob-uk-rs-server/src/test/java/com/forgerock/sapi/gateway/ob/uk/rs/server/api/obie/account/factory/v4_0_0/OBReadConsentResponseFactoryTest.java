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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.factory.v4_0_0;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.account.FRReadConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v4_0_0.accounts.AccountAccessConsentsApi;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import uk.org.openbanking.datamodel.v3.account.OBRisk2;
import uk.org.openbanking.datamodel.v4.account.*;
import uk.org.openbanking.datamodel.v4.account.OBInternalPermissions1Code;
import uk.org.openbanking.datamodel.v4.account.OBReadConsentStatus;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OBReadConsentResponseFactoryTest {

    private final OBReadConsentResponseFactory responseFactory = new OBReadConsentResponseFactory();

    @Test
    void buildConsentResponse() {
        final AccountAccessConsent consent = new AccountAccessConsent();
        final String consentId = "CONSENT-1223434";
        final String apiClientId = "test-client-1";
        final Date creationDateTime = new Date();
        consent.setId(consentId);
        consent.setStatus("AwaitingAuthorisation");
        consent.setApiClientId(apiClientId);
        consent.setCreationDateTime(creationDateTime);
        consent.setStatusUpdateDateTime(creationDateTime);
        final OBReadConsent1 consentRequest = new OBReadConsent1().data(
                        new OBReadConsent1Data().permissions(List.of(OBInternalPermissions1Code.READACCOUNTSBASIC, OBInternalPermissions1Code.READBALANCES))
                                .transactionToDateTime(DateTime.now())
                                .transactionFromDateTime(DateTime.now().minusDays(30))
                                .expirationDateTime(DateTime.now().plusDays(90)))
                        .risk(new OBRisk2());
        consent.setRequestObj(FRReadConsentConverter.toFRReadConsent(consentRequest));
        final OBReadConsentResponse1 obReadConsentResponse1 = responseFactory.buildConsentResponse(consent, AccountAccessConsentsApi.class);
        final OBReadConsentResponse1Data response1Data = obReadConsentResponse1.getData();
        assertThat(response1Data.getConsentId()).isEqualTo(consentId);
        assertThat(response1Data.getStatus()).isEqualTo(OBReadConsentStatus.AWAU);
        assertThat(response1Data.getPermissions()).isEqualTo(List.of(OBInternalPermissions1Code.READACCOUNTSBASIC, OBInternalPermissions1Code.READBALANCES));
        assertThat(response1Data.getCreationDateTime()).isEqualTo(new DateTime(creationDateTime));
        assertThat(response1Data.getStatusUpdateDateTime()).isEqualTo(new DateTime(creationDateTime));
        assertThat(response1Data.getExpirationDateTime()).isEqualTo(consentRequest.getData().getExpirationDateTime());
        assertThat(response1Data.getTransactionFromDateTime()).isEqualTo(consentRequest.getData().getTransactionFromDateTime());
        assertThat(response1Data.getTransactionFromDateTime()).isEqualTo(consentRequest.getData().getTransactionFromDateTime());
        assertThat(obReadConsentResponse1.getMeta()).isNotNull();
        assertThat(obReadConsentResponse1.getLinks().getSelf().toString()).endsWith("/open-banking/v4.0.0/aisp/account-access-consents/" + consentId);
    }
}