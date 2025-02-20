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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.domesticscheduledpayments;

import static com.forgerock.sapi.gateway.ob.uk.common.error.ErrorCode.OBRI_CONSENT_NOT_FOUND;
import static com.forgerock.sapi.gateway.ob.uk.common.error.ErrorCode.OBRI_PERMISSION_INVALID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.payment.FRWriteDomesticScheduledConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.error.ErrorCode;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException.ErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.domesticscheduled.DomesticScheduledPaymentConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticscheduled.v3_1_10.CreateDomesticScheduledPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticscheduled.v3_1_10.DomesticScheduledPaymentConsent;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

import jakarta.annotation.PostConstruct;
import uk.org.openbanking.datamodel.error.OBStandardErrorCodes1;
import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v3.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.v3.payment.OBPaymentConsentStatus;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticScheduledConsent4;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticScheduledConsentResponse5;
import uk.org.openbanking.testsupport.v3.payment.OBWriteDomesticScheduledConsentTestDataFactory;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class DomesticScheduledPaymentConsentsApiControllerTest {

    private static final String TEST_API_CLIENT_ID = "client_234093-49";

    private static final HttpHeaders HTTP_HEADERS = HttpHeadersTestDataFactory.requiredPaymentHttpHeaders(TEST_API_CLIENT_ID);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    @Qualifier("v3.1.10RestDomesticScheduledPaymentConsentStoreClient")
    private DomesticScheduledPaymentConsentStoreClient consentStoreClient;

    private String controllerBaseUri;

    @PostConstruct
    public void postConstruct() {
        controllerBaseUri = "http://localhost:" + port + "/open-banking/v3.1.10/pisp/domestic-scheduled-payment-consents";
    }


    @Test
    public void testCreateConsent() {
        final OBWriteDomesticScheduledConsent4 consentRequest = createValidateConsentRequest();
        final DomesticScheduledPaymentConsent consentStoreResponse = buildAwaitingAuthorisationConsent(consentRequest);
        when(consentStoreClient.createConsent(any())).thenAnswer(invocation -> {
            final CreateDomesticScheduledPaymentConsentRequest createConsentArg = invocation.getArgument(0, CreateDomesticScheduledPaymentConsentRequest.class);
            assertThat(createConsentArg.getApiClientId()).isEqualTo(TEST_API_CLIENT_ID);
            assertThat(createConsentArg.getConsentRequest()).isEqualTo(FRWriteDomesticScheduledConsentConverter.toFRWriteDomesticScheduledConsent(consentRequest));
            assertThat(createConsentArg.getCharges()).isEmpty();
            assertThat(createConsentArg.getIdempotencyKey()).isEqualTo(HTTP_HEADERS.getFirst("x-idempotency-key"));

            return consentStoreResponse;
        });

        final HttpEntity<OBWriteDomesticScheduledConsent4> entity = new HttpEntity<>(consentRequest, HTTP_HEADERS);

        final ResponseEntity<OBWriteDomesticScheduledConsentResponse5> createResponse = restTemplate.exchange(controllerBaseUri, HttpMethod.POST,
                                                                                                              entity, OBWriteDomesticScheduledConsentResponse5.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        final OBWriteDomesticScheduledConsentResponse5 consentResponse = createResponse.getBody();
        final String consentId = consentResponse.getData().getConsentId();
        assertThat(consentId).isEqualTo(consentStoreResponse.getId());
        assertThat(consentResponse.getData().getStatus()).isEqualTo(OBPaymentConsentStatus.AWAITINGAUTHORISATION);
        assertThat(consentResponse.getData().getInitiation()).isEqualTo(consentRequest.getData().getInitiation());
        assertThat(consentResponse.getData().getAuthorisation()).isEqualTo(consentRequest.getData().getAuthorisation());
        assertThat(consentResponse.getData().getScASupportData()).isEqualTo(consentRequest.getData().getScASupportData());
        assertThat(consentResponse.getData().getReadRefundAccount()).isEqualTo(consentRequest.getData().getReadRefundAccount());
        assertThat(consentResponse.getData().getCreationDateTime()).isNotNull();
        assertThat(consentResponse.getData().getStatusUpdateDateTime()).isNotNull();
        assertThat(consentResponse.getRisk()).isEqualTo(consentRequest.getRisk());
        final String selfLinkToConsent = consentResponse.getLinks().getSelf().toString();
        assertThat(selfLinkToConsent).isEqualTo(controllerGetConsentUri(consentId));

        // Get the consent and verify it matches the create response
        when(consentStoreClient.getConsent(eq(consentId), eq(TEST_API_CLIENT_ID))).thenReturn(consentStoreResponse);

        final ResponseEntity<OBWriteDomesticScheduledConsentResponse5> getConsentResponse = restTemplate.exchange(selfLinkToConsent,
                HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBWriteDomesticScheduledConsentResponse5.class);

        assertThat(getConsentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getConsentResponse.getBody()).isEqualTo(consentResponse);
    }

    @Test
    public void failsToCreateConsentIfRequestDoesNotPassJavaBeanValidation() {
        final OBWriteDomesticScheduledConsent4 emptyConsent = new OBWriteDomesticScheduledConsent4();

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

    @Test
    public void failsToCreateConsentIfRequestDoesNotPassBizLogicValidation() {
        final OBWriteDomesticScheduledConsent4 consentWithInvalidAmount = OBWriteDomesticScheduledConsentTestDataFactory.aValidOBWriteDomesticScheduledConsent4();
        consentWithInvalidAmount.getData().getInitiation().getInstructedAmount().setAmount("0"); // Invalid amount

        final ResponseEntity<OBErrorResponse1> response = restTemplate.exchange(controllerBaseUri, HttpMethod.POST,
                new HttpEntity<>(consentWithInvalidAmount, HTTP_HEADERS), OBErrorResponse1.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        final List<OBError1> errors = response.getBody().getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getErrorCode()).isEqualTo(ErrorCode.OBRI_DATA_REQUEST_INVALID.toString());
        assertThat(errors.get(0).getMessage()).isEqualTo("Your data request is invalid: reason The amount 0 provided must be greater than 0");

        verifyNoMoreInteractions(consentStoreClient);
    }

    @Test
    public void failsToGetConsentThatDoesNotExist() {
        when(consentStoreClient.getConsent(anyString(), anyString())).thenThrow(new ConsentStoreClientException(ErrorType.NOT_FOUND, "Consent Not Found"));
        final ResponseEntity<OBErrorResponse1> consentNotFoundResponse = restTemplate.exchange(controllerGetConsentUri("unknown"),
                HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBErrorResponse1.class);

        assertThat(consentNotFoundResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(consentNotFoundResponse.getBody().getCode()).isEqualTo(OBRI_CONSENT_NOT_FOUND.toString());
    }

    @Test
    public void failsToGetConsentInvalidPermissions() {
        when(consentStoreClient.getConsent(anyString(), anyString())).thenThrow(new ConsentStoreClientException(ErrorType.INVALID_PERMISSIONS, "ApiClient does not have permission to access Consent"));
        final ResponseEntity<OBErrorResponse1> consentNotFoundResponse = restTemplate.exchange(controllerGetConsentUri("unknown"),
                HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBErrorResponse1.class);

        assertThat(consentNotFoundResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(consentNotFoundResponse.getBody().getCode()).isEqualTo(OBRI_PERMISSION_INVALID.toString());
    }

    private String controllerGetConsentUri(String consentId) {
        return controllerBaseUri + "/" + consentId;
    }

    private static OBWriteDomesticScheduledConsent4 createValidateConsentRequest() {
        final OBWriteDomesticScheduledConsent4 consentRequest = OBWriteDomesticScheduledConsentTestDataFactory.aValidOBWriteDomesticScheduledConsent4();
        consentRequest.getData().getAuthorisation().setCompletionDateTime(DateTime.now(DateTimeZone.UTC));
        consentRequest.getData().getInitiation().setRequestedExecutionDateTime(DateTime.now(DateTimeZone.UTC).plusDays(2));
        return consentRequest;
    }

    public static DomesticScheduledPaymentConsent buildAwaitingAuthorisationConsent(OBWriteDomesticScheduledConsent4 consentRequest) {
        final DomesticScheduledPaymentConsent consentStoreResponse = new DomesticScheduledPaymentConsent();
        consentStoreResponse.setId(IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId());
        consentStoreResponse.setRequestObj(FRWriteDomesticScheduledConsentConverter.toFRWriteDomesticScheduledConsent(consentRequest));
        consentStoreResponse.setStatus(OBPaymentConsentStatus.AWAITINGAUTHORISATION.toString());
        consentStoreResponse.setCharges(List.of());
        final Date creationDateTime = new Date();
        consentStoreResponse.setCreationDateTime(creationDateTime);
        consentStoreResponse.setStatusUpdateDateTime(creationDateTime);
        return consentStoreResponse;
    }

}