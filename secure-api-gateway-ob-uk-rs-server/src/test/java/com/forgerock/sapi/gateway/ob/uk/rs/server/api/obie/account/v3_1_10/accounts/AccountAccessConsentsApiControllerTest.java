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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_10.accounts;

import static com.forgerock.sapi.gateway.ob.uk.common.error.ErrorCode.OBRI_CONSENT_NOT_FOUND;
import static com.forgerock.sapi.gateway.ob.uk.common.error.ErrorCode.OBRI_PERMISSION_INVALID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
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

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRReadConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException.ErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.client.account.v3_1_10.AccountAccessConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.CreateAccountAccessConsentRequest;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

import uk.org.openbanking.datamodel.account.OBReadConsent1;
import uk.org.openbanking.datamodel.account.OBReadConsent1Data;
import uk.org.openbanking.datamodel.account.OBReadConsentResponse1;
import uk.org.openbanking.datamodel.account.OBRisk2;
import uk.org.openbanking.datamodel.common.OBExternalPermissions1Code;
import uk.org.openbanking.datamodel.common.OBExternalRequestStatus1Code;
import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.error.OBStandardErrorCodes1;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
class AccountAccessConsentsApiControllerTest {

    private static final String TEST_API_CLIENT_ID = "client-34234234";

    private static final HttpHeaders HTTP_HEADERS = HttpHeadersTestDataFactory.requiredAccountConsentApiHeaders(TEST_API_CLIENT_ID);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private AccountAccessConsentStoreClient consentStoreClient;

    public String accountAccessConsentApiUri() {
        return "http://localhost:" + port + "/open-banking/v3.1.10/aisp/account-access-consents";
    }

    public String accountAccessConsentApiUri(String consentId) {
        return accountAccessConsentApiUri() + "/" + consentId;
    }

    @Test
    void testCreateAccountAccessConsent() {
        final OBReadConsent1 obConsentRequest = createValidateConsentRequest();

        final AccountAccessConsent mockConsentStoreResponse = mockAwaitingAuthorisationConsentStoreResponse(obConsentRequest);
        final String consentId = mockConsentStoreResponse.getId();

        final HttpEntity<OBReadConsent1> entity = new HttpEntity<>(obConsentRequest, HTTP_HEADERS);
        final ResponseEntity<OBReadConsentResponse1> createResponse = restTemplate.exchange(accountAccessConsentApiUri(), HttpMethod.POST,
                entity, OBReadConsentResponse1.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        final OBReadConsentResponse1 createConsentApiResponse = createResponse.getBody();
        assertThat(createConsentApiResponse.getData().getConsentId()).isEqualTo(mockConsentStoreResponse.getId());
        assertThat(createConsentApiResponse.getData().getStatus()).isEqualTo(OBExternalRequestStatus1Code.AWAITINGAUTHORISATION);
        assertThat(createConsentApiResponse.getData().getCreationDateTime()).isNotNull();
        assertThat(createConsentApiResponse.getData().getStatusUpdateDateTime()).isNotNull();
        assertThat(createConsentApiResponse.getData().getPermissions()).isEqualTo(obConsentRequest.getData().getPermissions());
        assertThat(createConsentApiResponse.getData().getExpirationDateTime()).isEqualTo(obConsentRequest.getData().getExpirationDateTime());

        final String selfLinkToConsent = createConsentApiResponse.getLinks().getSelf().toString();
        assertThat(selfLinkToConsent).isEqualTo(accountAccessConsentApiUri(consentId));

        // Get the consent and verify it matches the create response
        when(consentStoreClient.getConsent(eq(createConsentApiResponse.getData().getConsentId()), eq(TEST_API_CLIENT_ID))).thenReturn(mockConsentStoreResponse);

        final ResponseEntity<OBReadConsentResponse1> getConsentResponse = restTemplate.exchange(accountAccessConsentApiUri(createConsentApiResponse.getData().getConsentId()),
                HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBReadConsentResponse1.class);

        assertThat(getConsentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getConsentResponse.getBody()).isEqualTo(createConsentApiResponse);
    }

    private AccountAccessConsent mockAwaitingAuthorisationConsentStoreResponse(OBReadConsent1 obConsentRequest) {
        final AccountAccessConsent consentStoreResponse = buildAwaitingAuthorisationConsent(obConsentRequest);
        when(consentStoreClient.createConsent(any())).thenAnswer(invocation -> {
            final CreateAccountAccessConsentRequest createConsentArg = invocation.getArgument(0, CreateAccountAccessConsentRequest.class);
            assertThat(createConsentArg.getApiClientId()).isEqualTo(TEST_API_CLIENT_ID);
            assertThat(createConsentArg.getConsentRequest()).isEqualTo(FRReadConsentConverter.toFRReadConsent(obConsentRequest));

            return consentStoreResponse;
        });
        return consentStoreResponse;
    }

    @Test
    public void deleteConsent() {
        final String consentId = "consent-123";
        final ResponseEntity<Void> deleteResponse = restTemplate.exchange(accountAccessConsentApiUri(consentId), HttpMethod.DELETE,
                new HttpEntity<>(HTTP_HEADERS), Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(consentStoreClient).deleteConsent(eq(consentId), eq(TEST_API_CLIENT_ID));
        verifyNoMoreInteractions(consentStoreClient);
    }

    @Test
    public void failsToCreateConsentIfRequestDoesNotPassJavaBeanValidation() {
        final OBReadConsent1 obConsentRequest = createValidateConsentRequest();
        obConsentRequest.getData().setPermissions(null); // Omit required permissions field


        final HttpEntity<OBReadConsent1> entity = new HttpEntity<>(obConsentRequest, HTTP_HEADERS);
        final ResponseEntity<OBErrorResponse1> createResponse = restTemplate.exchange(accountAccessConsentApiUri(), HttpMethod.POST,
                entity, OBErrorResponse1.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        final List<OBError1> errors = createResponse.getBody().getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).isEqualTo(
                new OBError1().errorCode(OBStandardErrorCodes1.UK_OBIE_FIELD_INVALID.toString())
                              .message("The field received is invalid. Reason 'size must be between 1 and 2147483647'").path("data.permissions"));

        verifyNoMoreInteractions(consentStoreClient);
    }

    @Test
    public void failsToGetConsentThatDoesNotExist() {
        when(consentStoreClient.getConsent(anyString(), anyString())).thenThrow(new ConsentStoreClientException(ErrorType.NOT_FOUND, "Consent Not Found"));
        final ResponseEntity<OBErrorResponse1> consentNotFoundResponse = restTemplate.exchange(accountAccessConsentApiUri("unknown"),
                HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBErrorResponse1.class);

        assertThat(consentNotFoundResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(consentNotFoundResponse.getBody().getCode()).isEqualTo(OBRI_CONSENT_NOT_FOUND.toString());
    }

    @Test
    public void failsToGetConsentInvalidPermissions() {
        when(consentStoreClient.getConsent(anyString(), anyString())).thenThrow(new ConsentStoreClientException(ErrorType.INVALID_PERMISSIONS, "ApiClient does not have permission to access Consent"));
        final ResponseEntity<OBErrorResponse1> consentNotFoundResponse = restTemplate.exchange(accountAccessConsentApiUri("consent-231134"),
                HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBErrorResponse1.class);

        assertThat(consentNotFoundResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(consentNotFoundResponse.getBody().getCode()).isEqualTo(OBRI_PERMISSION_INVALID.toString());
    }

    private AccountAccessConsent buildAwaitingAuthorisationConsent(OBReadConsent1 obConsentRequest) {
        final AccountAccessConsent accountAccessConsent = new AccountAccessConsent();
        accountAccessConsent.setId(IntentType.ACCOUNT_ACCESS_CONSENT.generateIntentId());
        accountAccessConsent.setStatus(OBExternalRequestStatus1Code.AWAITINGAUTHORISATION.toString());
        accountAccessConsent.setRequestObj(FRReadConsentConverter.toFRReadConsent(obConsentRequest));
        accountAccessConsent.setApiClientId(TEST_API_CLIENT_ID);
        final Date creationDateTime = new Date();
        accountAccessConsent.setCreationDateTime(creationDateTime);
        accountAccessConsent.setStatusUpdateDateTime(creationDateTime);
        return accountAccessConsent;
    }

    private OBReadConsent1 createValidateConsentRequest() {
        return new OBReadConsent1().data(new OBReadConsent1Data().permissions(List.of(
                OBExternalPermissions1Code.READBENEFICIARIESBASIC,
                OBExternalPermissions1Code.READBALANCES))).risk(new OBRisk2());
    }


}