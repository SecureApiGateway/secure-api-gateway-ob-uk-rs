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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.funds.v3_1_10;

import static com.forgerock.sapi.gateway.ob.uk.common.error.ErrorCode.OBRI_CONSENT_NOT_FOUND;
import static com.forgerock.sapi.gateway.ob.uk.common.error.ErrorCode.OBRI_PERMISSION_INVALID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.funds.FRFundsConfirmationConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException;
import com.forgerock.sapi.gateway.rcs.consent.store.client.funds.v3_1_10.FundsConfirmationConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.CreateFundsConfirmationConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.FundsConfirmationConsent;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

import uk.org.openbanking.datamodel.v3.common.OBExternalRequestStatus1Code;
import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v3.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.v3.error.OBStandardErrorCodes1;
import uk.org.openbanking.datamodel.v3.fund.OBFundsConfirmationConsent1;
import uk.org.openbanking.datamodel.v3.fund.OBFundsConfirmationConsent1Data;
import uk.org.openbanking.datamodel.v3.fund.OBFundsConfirmationConsent1DataDebtorAccount;
import uk.org.openbanking.datamodel.v3.fund.OBFundsConfirmationConsentResponse1;
import uk.org.openbanking.datamodel.v3.fund.OBFundsConfirmationConsentResponse1Data.StatusEnum;

/**
 * Test for {@link FundsConfirmationConsentsApiController}
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class FundsConfirmationConsentsApiControllerTest {
    private static final String TEST_API_CLIENT_ID = UUID.randomUUID().toString();

    private static final HttpHeaders HTTP_HEADERS = HttpHeadersTestDataFactory.requiredFundsConsentApiHeaders(TEST_API_CLIENT_ID);
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private FundsConfirmationConsentStoreClient consentStoreClient;

    public String fundsConfirmationConsentApiUri() {
        return "http://localhost:" + port + "/open-banking/" + OBVersion.v3_1_10.getCanonicalName() + "/cbpii/funds-confirmation-consents";
    }

    public String fundsConfirmationConsentApiUri(String consentId) {
        return fundsConfirmationConsentApiUri() + "/" + consentId;
    }

    @Test
    void shouldCreateFundsConfirmationConsent() {
        final OBFundsConfirmationConsent1 obConsentRequest = createValidOBConsentRequest();
        final FundsConfirmationConsent mockConsentStoreResponse = mockAwaitingAuthorisationConsentStoreResponse(obConsentRequest);
        final String consentId = mockConsentStoreResponse.getId();
        final HttpEntity<OBFundsConfirmationConsent1> entity = new HttpEntity<>(obConsentRequest, HTTP_HEADERS);
        final ResponseEntity<OBFundsConfirmationConsentResponse1> createResponse = restTemplate.exchange(
                fundsConfirmationConsentApiUri(),
                HttpMethod.POST,
                entity,
                OBFundsConfirmationConsentResponse1.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        final OBFundsConfirmationConsentResponse1 createConsentApiResponse = createResponse.getBody();
        assertThat(createConsentApiResponse.getData().getConsentId()).isEqualTo(mockConsentStoreResponse.getId());
        assertThat(createConsentApiResponse.getData().getStatus()).isEqualTo(StatusEnum.AWAITINGAUTHORISATION);
        assertThat(createConsentApiResponse.getData().getCreationDateTime()).isNotNull();
        assertThat(createConsentApiResponse.getData().getStatusUpdateDateTime()).isNotNull();
        assertThat(createConsentApiResponse.getData().getExpirationDateTime()).isEqualTo(obConsentRequest.getData().getExpirationDateTime());
        assertThat(createConsentApiResponse.getData().getDebtorAccount()).isEqualTo(obConsentRequest.getData().getDebtorAccount());


        final String selfLinkToConsent = createConsentApiResponse.getLinks().getSelf().toString();
        assertThat(selfLinkToConsent).isEqualTo(fundsConfirmationConsentApiUri(consentId));

        // Get the consent and verify it matches the created response
        when(consentStoreClient.getConsent(eq(createConsentApiResponse.getData().getConsentId()), eq(TEST_API_CLIENT_ID))).thenReturn(mockConsentStoreResponse);

        final ResponseEntity<OBFundsConfirmationConsentResponse1> getConsentResponse = restTemplate.exchange(fundsConfirmationConsentApiUri(createConsentApiResponse.getData().getConsentId()),
                HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBFundsConfirmationConsentResponse1.class);

        assertThat(getConsentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getConsentResponse.getBody()).isEqualTo(createConsentApiResponse);
    }

    @Test
    void shouldDeleteConsent() {
        final String consentId = UUID.randomUUID().toString();
        final ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                fundsConfirmationConsentApiUri(consentId),
                HttpMethod.DELETE,
                new HttpEntity<>(HTTP_HEADERS),
                Void.class
        );
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(consentStoreClient).deleteConsent(eq(consentId), eq(TEST_API_CLIENT_ID));
        verifyNoMoreInteractions(consentStoreClient);
    }

    @Test
    void failsToCreateConsentIfRequestDoesNotPassJavaBeanValidation() {
        final OBFundsConfirmationConsent1 confirmationConsent1Request = createValidOBConsentRequest();
        confirmationConsent1Request.getData().setDebtorAccount(null); // required debtor account
        final HttpEntity<OBFundsConfirmationConsent1> entity = new HttpEntity<>(confirmationConsent1Request, HTTP_HEADERS);
        final ResponseEntity<OBErrorResponse1> createResponse = restTemplate.exchange(
                fundsConfirmationConsentApiUri(),
                HttpMethod.POST,
                entity,
                OBErrorResponse1.class
        );
        final List<OBError1> errors = createResponse.getBody().getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).isEqualTo(
                new OBError1().errorCode(OBStandardErrorCodes1.UK_OBIE_FIELD_INVALID.toString())
                        .message("The field received is invalid. Reason 'must not be null'").path("data.debtorAccount"));

        verifyNoMoreInteractions(consentStoreClient);
    }

    @Test
    public void failsToGetConsentThatDoesNotExist() {
        when(consentStoreClient.getConsent(anyString(), anyString())).thenThrow(new ConsentStoreClientException(ConsentStoreClientException.ErrorType.NOT_FOUND, "Consent Not Found"));
        final ResponseEntity<OBErrorResponse1> consentNotFoundResponse = restTemplate.exchange(fundsConfirmationConsentApiUri("unknown"),
                HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBErrorResponse1.class);

        assertThat(consentNotFoundResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(consentNotFoundResponse.getBody().getCode()).isEqualTo(OBRI_CONSENT_NOT_FOUND.toString());
    }

    @Test
    public void failsToGetConsentInvalidPermissions() {
        when(consentStoreClient.getConsent(anyString(), anyString())).thenThrow(new ConsentStoreClientException(ConsentStoreClientException.ErrorType.INVALID_PERMISSIONS, "ApiClient does not have permission to access Consent"));
        final ResponseEntity<OBErrorResponse1> consentNotFoundResponse = restTemplate.exchange(fundsConfirmationConsentApiUri(IntentType.FUNDS_CONFIRMATION_CONSENT.generateIntentId()),
                HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBErrorResponse1.class);

        assertThat(consentNotFoundResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(consentNotFoundResponse.getBody().getCode()).isEqualTo(OBRI_PERMISSION_INVALID.toString());
    }

    private FundsConfirmationConsent mockAwaitingAuthorisationConsentStoreResponse(OBFundsConfirmationConsent1 obConsentRequest) {
        final FundsConfirmationConsent consentStoreResponse = buildAwaitingAuthorisationConsent(obConsentRequest);
        when(consentStoreClient.createConsent(any())).thenAnswer(invocation -> {
            final CreateFundsConfirmationConsentRequest createConsentArg = invocation.getArgument(0, CreateFundsConfirmationConsentRequest.class);
            assertThat(createConsentArg.getApiClientId()).isEqualTo(TEST_API_CLIENT_ID);
            assertThat(createConsentArg.getConsentRequest()).isEqualTo(FRFundsConfirmationConsentConverter.toFRFundsConfirmationConsent(obConsentRequest));

            return consentStoreResponse;
        });
        return consentStoreResponse;
    }

    private FundsConfirmationConsent buildAwaitingAuthorisationConsent(OBFundsConfirmationConsent1 obConsentRequest) {
        final FundsConfirmationConsent fundsConfirmationConsent = new FundsConfirmationConsent();
        fundsConfirmationConsent.setId(IntentType.FUNDS_CONFIRMATION_CONSENT.generateIntentId());
        fundsConfirmationConsent.setStatus(OBExternalRequestStatus1Code.AWAITINGAUTHORISATION.toString());
        fundsConfirmationConsent.setRequestObj(FRFundsConfirmationConsentConverter.toFRFundsConfirmationConsent(obConsentRequest));
        fundsConfirmationConsent.setApiClientId(TEST_API_CLIENT_ID);
        final Date creationDateTime = new Date();
        fundsConfirmationConsent.setCreationDateTime(creationDateTime);
        fundsConfirmationConsent.setStatusUpdateDateTime(creationDateTime);
        return fundsConfirmationConsent;
    }

    private OBFundsConfirmationConsent1 createValidOBConsentRequest() {
        return new OBFundsConfirmationConsent1()
                .data(
                        new OBFundsConfirmationConsent1Data()
                                .expirationDateTime(DateTime.now().plusDays(30).withZone(DateTimeZone.UTC))
                                .debtorAccount(
                                        new OBFundsConfirmationConsent1DataDebtorAccount()
                                                .schemeName("UK.OBIE.SortCodeAccountNumber")
                                                .identification("40400422390112")
                                                .name("Mrs B Smith")
                                )
                );
    }
}
