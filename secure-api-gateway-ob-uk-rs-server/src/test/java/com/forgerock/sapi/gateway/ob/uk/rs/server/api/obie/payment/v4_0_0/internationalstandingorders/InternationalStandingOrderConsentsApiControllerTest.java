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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v4_0_0.internationalstandingorders;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRWriteInternationalStandingOrderConsentConverter.toFRWriteInternationalStandingOrderConsent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.internationalstandingorder.InternationalStandingOrderConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.internationalstandingorder.v3_1_10.InternationalStandingOrderConsent;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

import jakarta.annotation.PostConstruct;
import uk.org.openbanking.datamodel.error.OBStandardErrorCodes1;
import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v3.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.v3.payment.OBPaymentConsentStatus;
import uk.org.openbanking.datamodel.v4.payment.OBWriteInternationalScheduledConsent5;
import uk.org.openbanking.datamodel.v4.payment.OBWriteInternationalStandingOrderConsent6;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class InternationalStandingOrderConsentsApiControllerTest {

    private static final String TEST_API_CLIENT_ID = "client_234093-49";

    private static final HttpHeaders HTTP_HEADERS = HttpHeadersTestDataFactory.requiredPaymentHttpHeaders(TEST_API_CLIENT_ID);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    @Qualifier("v4.0.0RestInternationalStandingOrderConsentStoreClient")
    private InternationalStandingOrderConsentStoreClient consentStoreClient;

    private String controllerBaseUri;

    @PostConstruct
    public void postConstruct() {
        controllerBaseUri = "http://localhost:" + port + "/open-banking/v4.0.0/pisp/international-standing-order-consents";
    }

    private String controllerGetConsentUri(String consentId) {
        return controllerBaseUri + "/" + consentId;
    }

    public static InternationalStandingOrderConsent buildAwaitingAuthorisationConsent(OBWriteInternationalStandingOrderConsent6 consentRequest) {
        final InternationalStandingOrderConsent consentStoreResponse = new InternationalStandingOrderConsent();
        consentStoreResponse.setId(IntentType.PAYMENT_INTERNATIONAL_CONSENT.generateIntentId());
        consentStoreResponse.setRequestObj(toFRWriteInternationalStandingOrderConsent(consentRequest));
        consentStoreResponse.setStatus(OBPaymentConsentStatus.AWAITINGAUTHORISATION.toString());
        consentStoreResponse.setCharges(List.of());
        final Date creationDateTime = new Date();
        consentStoreResponse.setCreationDateTime(creationDateTime);
        consentStoreResponse.setStatusUpdateDateTime(creationDateTime);
        return consentStoreResponse;
    }

    @Test
    public void failsToCreateConsentIfRequestDoesNotPassJavaBeanValidation() {
        final OBWriteInternationalScheduledConsent5 emptyConsent = new OBWriteInternationalScheduledConsent5();

        final ResponseEntity<OBErrorResponse1> response = restTemplate.exchange(controllerBaseUri, HttpMethod.POST,
                new HttpEntity<>(emptyConsent, HTTP_HEADERS), OBErrorResponse1.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        final List<OBError1> errors = response.getBody().getErrors();
        assertThat(errors).hasSize(2);
        final String fieldMustNotBeNullErrMsg = "The field received is invalid. Reason 'must not be null'";
        final String fieldInvalidErrCode = OBStandardErrorCodes1.UK_OBIE_FIELD_INVALID.toString();
        assertThat(errors).containsExactlyInAnyOrder(new OBError1().errorCode(fieldInvalidErrCode).message(fieldMustNotBeNullErrMsg).path("risk"),
                new OBError1().errorCode(fieldInvalidErrCode).message(fieldMustNotBeNullErrMsg).path("data"));

        verifyNoMoreInteractions(consentStoreClient);
    }

    //TODO: Add more unit tests
}
