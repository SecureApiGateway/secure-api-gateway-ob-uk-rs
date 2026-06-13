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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v4_0_0.domesticpayments;

import static com.forgerock.sapi.gateway.ob.uk.common.error.ErrorCode.OBRI_CONSENT_NOT_FOUND;
import static com.forgerock.sapi.gateway.ob.uk.common.error.ErrorCode.OBRI_PERMISSION_INVALID;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.FundsConfirmationTestHelpers.validateConsentNotAuthorisedErrorResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
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

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRWriteDomesticConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.error.ErrorCode;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.balance.FundsAvailabilityService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException.ErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.domestic.DomesticPaymentConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domestic.v3_1_10.CreateDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domestic.v3_1_10.DomesticPaymentConsent;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

import jakarta.annotation.PostConstruct;
import uk.org.openbanking.datamodel.error.OBStandardErrorCodes1;
import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v3.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.v4.payment.OBPaymentConsentStatus;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticConsent4;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticConsentResponse5;
import uk.org.openbanking.datamodel.v4.payment.OBWriteFundsConfirmationResponse1;
import uk.org.openbanking.testsupport.v4.payment.OBWriteDomesticConsentTestDataFactory;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class DomesticPaymentConsentsApiControllerTest {

    private static final String TEST_API_CLIENT_ID = "client_234093-49";

    private static final HttpHeaders HTTP_HEADERS = HttpHeadersTestDataFactory.requiredPaymentHttpHeaders(TEST_API_CLIENT_ID);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    @Qualifier("v4.0.0RestDomesticPaymentConsentStoreClient")
    private DomesticPaymentConsentStoreClient consentStoreClient;

    @MockBean
    private FundsAvailabilityService fundsAvailabilityService;

    private String controllerBaseUri;

    @PostConstruct
    public void postConstruct() {
        controllerBaseUri = "http://localhost:" + port + "/open-banking/v4.0.0/pisp/domestic-payment-consents";
    }


    @Test
    public void testCreateConsent() {
        final OBWriteDomesticConsent4 consentRequest = createValidateConsentRequest();
        final DomesticPaymentConsent consentStoreResponse = buildAwaitingAuthorisationConsent(consentRequest);
        when(consentStoreClient.createConsent(any())).thenAnswer(invocation -> {
            final CreateDomesticPaymentConsentRequest createConsentArg = invocation.getArgument(0, CreateDomesticPaymentConsentRequest.class);
            assertThat(createConsentArg.getApiClientId()).isEqualTo(TEST_API_CLIENT_ID);
            assertThat(createConsentArg.getConsentRequest()).isEqualTo(FRWriteDomesticConsentConverter.toFRWriteDomesticConsent(consentRequest));
            assertThat(createConsentArg.getCharges()).isEmpty();
            assertThat(createConsentArg.getIdempotencyKey()).isEqualTo(HTTP_HEADERS.getFirst("x-idempotency-key"));

            return consentStoreResponse;
        });

        final HttpEntity<OBWriteDomesticConsent4> entity = new HttpEntity<>(consentRequest, HTTP_HEADERS);

        final ResponseEntity<OBWriteDomesticConsentResponse5> createResponse = restTemplate.exchange(controllerBaseUri, HttpMethod.POST,
                entity, OBWriteDomesticConsentResponse5.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        final OBWriteDomesticConsentResponse5 consentResponse = createResponse.getBody();
        final String consentId = consentResponse.getData().getConsentId();
        assertThat(consentId).isEqualTo(consentStoreResponse.getId());
        assertThat(consentResponse.getData().getStatus()).isEqualTo(OBPaymentConsentStatus.AWAU);
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

        final ResponseEntity<OBWriteDomesticConsentResponse5> getConsentResponse = restTemplate.exchange(selfLinkToConsent,
                HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBWriteDomesticConsentResponse5.class);

        assertThat(getConsentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getConsentResponse.getBody()).isEqualTo(consentResponse);
    }

    @Test
    public void failsToCreateConsentIfRequestDoesNotPassJavaBeanValidation() {
        final OBWriteDomesticConsent4 emptyConsent = new OBWriteDomesticConsent4();

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
        final OBWriteDomesticConsent4 consentWithInvalidAmount = OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomesticConsent4();
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

    @Test
    public void testFundsConfirmation() {
        final String accountId = "acc-1234344";
        final OBWriteDomesticConsent4 consentRequest = createValidateConsentRequest();
        final DomesticPaymentConsent consentStoreResponse = buildAuthorisedConsent(consentRequest, accountId);
        when(consentStoreClient.getConsent(eq(consentStoreResponse.getId()), eq(TEST_API_CLIENT_ID))).thenReturn(consentStoreResponse);
        when(fundsAvailabilityService.isFundsAvailable(eq(accountId), any())).thenReturn(Boolean.TRUE);

        final String fundsConfirmationUri = controllerFundsConfirmationUri(consentStoreResponse.getId());
        final ResponseEntity<OBWriteFundsConfirmationResponse1> fundsConfirmationResponse = restTemplate.exchange(fundsConfirmationUri,
                HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBWriteFundsConfirmationResponse1.class);

        assertThat(fundsConfirmationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        final OBWriteFundsConfirmationResponse1 fundsConfirmationResponseBody = fundsConfirmationResponse.getBody();
        assertThat(fundsConfirmationResponseBody.getData().getFundsAvailableResult().getFundsAvailable()).isTrue();
        assertThat(fundsConfirmationResponseBody.getLinks().getSelf().toString()).isEqualTo(fundsConfirmationUri);
    }

    @Test
    public void failsToGetFundsConfirmationWhenConsentNotAuthorised() {
        final OBWriteDomesticConsent4 consentRequest = createValidateConsentRequest();
        final DomesticPaymentConsent consentAwaitingAuthorisation = buildAwaitingAuthorisationConsent(consentRequest);
        when(consentStoreClient.getConsent(eq(consentAwaitingAuthorisation.getId()), eq(TEST_API_CLIENT_ID))).thenReturn(consentAwaitingAuthorisation);

        final String fundsConfirmationUri = controllerFundsConfirmationUri(consentAwaitingAuthorisation.getId());
        final ResponseEntity<OBErrorResponse1> fundsConfirmationResponse = restTemplate.exchange(fundsConfirmationUri,
                HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBErrorResponse1.class);

        validateConsentNotAuthorisedErrorResponse(fundsConfirmationResponse);

        verifyNoInteractions(fundsAvailabilityService);
    }

    private String controllerGetConsentUri(String consentId) {
        return controllerBaseUri + "/" + consentId;
    }

    private String controllerFundsConfirmationUri(String consentId) {
        return controllerGetConsentUri(consentId) + "/funds-confirmation";
    }

    private static OBWriteDomesticConsent4 createValidateConsentRequest() {
        final OBWriteDomesticConsent4 consentRequest = OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomesticConsent4();
        consentRequest.getData().getAuthorisation().setCompletionDateTime(DateTime.now(DateTimeZone.UTC));
        return consentRequest;
    }

    public static DomesticPaymentConsent buildAwaitingAuthorisationConsent(OBWriteDomesticConsent4 consentRequest) {
        final DomesticPaymentConsent consentStoreResponse = new DomesticPaymentConsent();
        consentStoreResponse.setId(IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId());
        consentStoreResponse.setRequestObj(FRWriteDomesticConsentConverter.toFRWriteDomesticConsent(consentRequest));
        consentStoreResponse.setStatus(OBPaymentConsentStatus.AWAU.toString());
        consentStoreResponse.setCharges(List.of());
        final Date creationDateTime = new Date();
        consentStoreResponse.setCreationDateTime(creationDateTime);
        consentStoreResponse.setStatusUpdateDateTime(creationDateTime);
        return consentStoreResponse;
    }

    private static DomesticPaymentConsent buildAuthorisedConsent(OBWriteDomesticConsent4 consentRequest, String debtorAccountId) {
        final DomesticPaymentConsent domesticPaymentConsent = buildAwaitingAuthorisationConsent(consentRequest);
        domesticPaymentConsent.setStatus(OBPaymentConsentStatus.AUTH.toString());
        domesticPaymentConsent.setAuthorisedDebtorAccountId(debtorAccountId);
        return domesticPaymentConsent;
    }
}