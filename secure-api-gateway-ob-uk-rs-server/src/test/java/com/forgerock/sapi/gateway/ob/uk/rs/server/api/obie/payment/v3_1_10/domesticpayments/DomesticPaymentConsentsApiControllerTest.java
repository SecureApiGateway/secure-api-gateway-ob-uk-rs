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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.domesticpayments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.List;

import javax.annotation.PostConstruct;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;
import com.forgerock.sapi.gateway.rcs.conent.store.client.v3_1_10.DomesticPaymentConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.v3_1_10.CreateDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.v3_1_10.DomesticPaymentConsent;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsent4;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5Data.StatusEnum;
import uk.org.openbanking.testsupport.payment.OBWriteDomesticConsentTestDataFactory;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
class DomesticPaymentConsentsApiControllerTest {


    private static final String TEST_API_CLIENT_ID = "client_234093-49";

    private static final HttpHeaders HTTP_HEADERS = HttpHeadersTestDataFactory.requiredPaymentHttpHeadersWithApiClientId(TEST_API_CLIENT_ID);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private DomesticPaymentConsentStoreClient consentStoreClient;

    private String controllerBaseUri;

    @PostConstruct
    public void postConstruct() {
        controllerBaseUri = "http://localhost:" + port + "/open-banking/v3.1.10/pisp/domestic-payment-consents";
    }


    @Test
    public void testCreateConsent() {
        final OBWriteDomesticConsent4 consentRequest = OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomesticConsent4();
        consentRequest.getData().getAuthorisation().setCompletionDateTime(DateTime.now(DateTimeZone.UTC));

        final DomesticPaymentConsent consentStoreResponse = createConsentStoreResponse(consentRequest);

        when(consentStoreClient.createConsent(any())).thenAnswer(invocation -> {
            final CreateDomesticPaymentConsentRequest createConsentArg = invocation.getArgument(0, CreateDomesticPaymentConsentRequest.class);
            assertThat(createConsentArg.getApiClientId()).isEqualTo(TEST_API_CLIENT_ID);
            assertThat(createConsentArg.getConsentRequest()).isEqualTo(consentRequest);
            assertThat(createConsentArg.getCharges()).isEmpty();
            assertThat(createConsentArg.getIdempotencyKey()).isEqualTo(HTTP_HEADERS.getFirst("x-idempotency-key"));

            return consentStoreResponse;
        });


        final HttpEntity<OBWriteDomesticConsent4> entity = new HttpEntity<>(consentRequest, HTTP_HEADERS);

        final ResponseEntity<OBWriteDomesticConsentResponse5> response = restTemplate.exchange(controllerBaseUri, HttpMethod.POST, entity, OBWriteDomesticConsentResponse5.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        final OBWriteDomesticConsentResponse5 responseBody = response.getBody();
        final String consentId = responseBody.getData().getConsentId();
        assertThat(consentId).isEqualTo(consentStoreResponse.getId());
        assertThat(responseBody.getData().getStatus()).isEqualTo(StatusEnum.AWAITINGAUTHORISATION);
        assertThat(responseBody.getData().getInitiation()).isEqualTo(consentRequest.getData().getInitiation());
        assertThat(responseBody.getData().getAuthorisation()).isEqualTo(consentRequest.getData().getAuthorisation());
        assertThat(responseBody.getData().getScASupportData()).isEqualTo(consentRequest.getData().getScASupportData());
        assertThat(responseBody.getData().getReadRefundAccount()).isEqualTo(consentRequest.getData().getReadRefundAccount());
        assertThat(responseBody.getData().getCreationDateTime()).isNotNull();
        assertThat(responseBody.getData().getStatusUpdateDateTime()).isNotNull();
        assertThat(responseBody.getRisk()).isEqualTo(consentRequest.getRisk());
        assertThat(responseBody.getLinks().getSelf().toString()).isEqualTo(controllerGetConsentUri(consentId));
    }

    private String controllerGetConsentUri(String consentId) {
        return controllerBaseUri + "/" + consentId;
    }

    private static DomesticPaymentConsent createConsentStoreResponse(OBWriteDomesticConsent4 consentRequest) {
        final DomesticPaymentConsent consentStoreResponse = new DomesticPaymentConsent();
        consentStoreResponse.setId(IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId());
        consentStoreResponse.setRequestObj(consentRequest);
        consentStoreResponse.setStatus(StatusEnum.AWAITINGAUTHORISATION.toString());
        consentStoreResponse.setCharges(List.of());
        final DateTime creationDateTime = DateTime.now();
        consentStoreResponse.setCreationDateTime(creationDateTime);
        consentStoreResponse.setStatusUpdateDateTime(creationDateTime);
        return consentStoreResponse;
    }
}