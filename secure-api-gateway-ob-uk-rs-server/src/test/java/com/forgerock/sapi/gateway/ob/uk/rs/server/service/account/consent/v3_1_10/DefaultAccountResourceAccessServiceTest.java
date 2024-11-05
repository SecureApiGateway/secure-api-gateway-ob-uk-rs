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
package com.forgerock.sapi.gateway.ob.uk.rs.server.service.account.consent.v3_1_10;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.client.account.AccountAccessConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DefaultAccountResourceAccessServiceTest {

    final String consentId = "consent-3456546546";
    final String apiClientId = "client-24234243";

    final List<String> authorisedAccountIds = List.of("acc-1", "acc-2", "acc-3");

    @Mock
    @Qualifier("v3.1.10RestAccountAccessConsentStoreClient")
    private AccountAccessConsentStoreClient consentStoreClient;

    @InjectMocks
    @Qualifier("v3.1.10DefaultAccountResourceAccessService")
    private DefaultAccountResourceAccessService accountAccessConsentService;


    @Test
    void testConsentNotAuthorised() {
        final AccountAccessConsent consent = new AccountAccessConsent();
        consent.setStatus("AwaitingAuthorisation");
        given(consentStoreClient.getConsent(eq(consentId), eq(apiClientId))).willReturn(consent);

        final OBErrorException exception = assertThrows(OBErrorException.class,
                () -> accountAccessConsentService.getConsentForResourceAccess(consentId, apiClientId));
        assertThat(exception.getObriErrorType()).isEqualTo(OBRIErrorType.CONSENT_STATUS_NOT_AUTHORISED);
    }

    @Test
    void testGetConsentForResourceAccessAuthorised() throws OBErrorException {
        final AccountAccessConsent consentStoreResponse = mockAuthorisedConsentResponse();

        final AccountAccessConsent consent = accountAccessConsentService.getConsentForResourceAccess(consentId, apiClientId);
        assertThat(consent).isEqualTo(consentStoreResponse);
    }

    private AccountAccessConsent mockAuthorisedConsentResponse() {
        final AccountAccessConsent consentStoreResponse = new AccountAccessConsent();
        consentStoreResponse.setStatus("Authorised");
        consentStoreResponse.setAuthorisedAccountIds(authorisedAccountIds);
        given(consentStoreClient.getConsent(eq(consentId), eq(apiClientId))).willReturn(consentStoreResponse);
        return consentStoreResponse;
    }


    @Test
    void testGetConsentForResourceAccessAuthorisedAccountNotAuthorised() {
        mockAuthorisedConsentResponse();

        final OBErrorException exception = assertThrows(OBErrorException.class,
                () -> accountAccessConsentService.getConsentForResourceAccess(consentId, apiClientId, "acc-not-authorised"));
        assertThat(exception.getObriErrorType()).isEqualTo(OBRIErrorType.UNAUTHORISED_ACCOUNT);
    }

    @Test
    void testGetConsentWithAuthorisedAccountCheck() throws OBErrorException {
        final AccountAccessConsent mockConsentStoreResponse = mockAuthorisedConsentResponse();

        for (String authorisedAccountId : authorisedAccountIds) {
            assertThat(accountAccessConsentService.getConsentForResourceAccess(consentId, apiClientId, authorisedAccountId))
                    .isEqualTo(mockConsentStoreResponse);
        }
    }

}