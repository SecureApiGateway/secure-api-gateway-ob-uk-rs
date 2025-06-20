/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rs.server.service.migration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.vrp.v3_1_10.DomesticVRPConsent;

import uk.org.openbanking.datamodel.v4.vrp.OBDomesticVRPConsentRequest;

@ExtendWith(MockitoExtension.class)
public class ConsentComparisonServiceTest {

    @InjectMocks
    private ConsentComparisonService consentComparisonService;

    @Test
    void testDoesRequestMatchConsent_BothNull() {
        boolean result = consentComparisonService.doesRequestMatchConsent(null, null);
        assertThat(result).isFalse();
    }

    @Test
    void testDoesRequestMatchConsent_RequestNull() {
        DomesticVRPConsent consent = new DomesticVRPConsent();
        boolean result = consentComparisonService.doesRequestMatchConsent(null, consent);
        assertThat(result).isFalse();
    }

    @Test
    void testDoesRequestMatchConsent_ConsentNull() {
        OBDomesticVRPConsentRequest request = new OBDomesticVRPConsentRequest();
        boolean result = consentComparisonService.doesRequestMatchConsent(request, null);
        assertThat(result).isFalse();
    }

    @Test
    void testDoesRequestMatchConsent_NotEqual() {
        OBDomesticVRPConsentRequest request = new OBDomesticVRPConsentRequest();
        DomesticVRPConsent consent = new DomesticVRPConsent();
        boolean result = consentComparisonService.doesRequestMatchConsent(request, consent);
        assertThat(result).isFalse();
    }
}
