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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.math.BigDecimal;

import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteFileConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.DefaultPaymentFileType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.file.v3_1_10.FilePaymentConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.ConsumePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.file.v3_1_10.FilePaymentConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments.FilePaymentSubmissionRepository;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

import uk.org.openbanking.datamodel.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5Data.StatusEnum;
import uk.org.openbanking.datamodel.payment.OBWriteFile2;
import uk.org.openbanking.datamodel.payment.OBWriteFile2Data;
import uk.org.openbanking.datamodel.payment.OBWriteFileConsent3;
import uk.org.openbanking.datamodel.payment.OBWriteFileConsentResponse4Data;
import uk.org.openbanking.datamodel.payment.OBWriteFileResponse3;
import uk.org.openbanking.datamodel.payment.OBWriteFileResponse3Data;
import uk.org.openbanking.testsupport.payment.OBWriteFileConsentTestDataFactory;

/**
 * A SpringBoot test for the {@link FilePaymentsApiController}.<br/>
 * Coverage versions v3.1.5 to v3.1.10.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class FilePaymentsApiControllerTest {

    private static final String TEST_API_CLIENT_ID = "client-123";
    private static final HttpHeaders HTTP_HEADERS = HttpHeadersTestDataFactory.requiredPaymentsHttpHeadersWithApiClientId(TEST_API_CLIENT_ID);
    private static final String BASE_URL = "http://localhost:";
    private static final String FILE_PAYMENTS_URI = "/open-banking/v3.1.10/pisp/file-payments";

    private final String debtorAccountId = "debtor-acc-123";

    @LocalServerPort
    private int port;

    @Autowired
    private FilePaymentSubmissionRepository filePaymentsRepository;

    @MockBean
    private FilePaymentConsentStoreClient filePaymentConsentStoreClient;

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterEach
    void removeData() {
        filePaymentsRepository.deleteAll();
    }

    private static OBWriteFileConsent3 createValidConsent() {
        final OBWriteFileConsent3 consent = OBWriteFileConsentTestDataFactory.aValidOBWriteFileConsent3(
                DefaultPaymentFileType.UK_OBIE_PAIN_001.getPaymentFileType().getFileType(),
                "hash123", "12", BigDecimal.ONE);
        consent.getData().getInitiation().setRequestedExecutionDateTime(
                consent.getData().getInitiation().getRequestedExecutionDateTime().withZone(DateTimeZone.UTC));
        return consent;
    }

    private void mockConsentStoreGetResponse(String consentId, OBWriteFileConsent3 consentRequest) {
        mockConsentStoreGetResponse(consentId, consentRequest, StatusEnum.AUTHORISED.toString());
    }

    private void mockConsentStoreGetResponse(String consentId, OBWriteFileConsent3 consentRequest, String status) {
        final FilePaymentConsent consent = new FilePaymentConsent();
        consent.setId(consentId);
        consent.setStatus(status);
        consent.setRequestObj(FRWriteFileConsentConverter.toFRWriteFileConsent(consentRequest));
        consent.setAuthorisedDebtorAccountId(debtorAccountId);
        when(filePaymentConsentStoreClient.getConsent(eq(consentId), eq(TEST_API_CLIENT_ID))).thenReturn(consent);
    }

    @Test
    public void shouldCreateFilePayment() {
        final String consentId = IntentType.PAYMENT_FILE_CONSENT.generateIntentId();
        final OBWriteFileConsent3 filePaymentConsent = createValidConsent();
        mockConsentStoreGetResponse(consentId, filePaymentConsent);

        final OBWriteFile2 filePayment = createPayment(consentId, filePaymentConsent);
        final HttpEntity<OBWriteFile2> filePaymentEntity = new HttpEntity<>(filePayment, HTTP_HEADERS);

        final ResponseEntity<OBWriteFileResponse3> filePaymentResponse = restTemplate.exchange(filePaymentsUrl(), HttpMethod.POST, filePaymentEntity, OBWriteFileResponse3.class);

        assertThat(filePaymentResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBWriteFileResponse3Data responseData = filePaymentResponse.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(consentId);
        assertThat(responseData.getInitiation()).isEqualTo(filePayment.getData().getInitiation());
        assertThat(filePaymentResponse.getBody().getLinks().getSelf().toString()).isEqualTo(filePaymentsIdUrl(responseData.getFilePaymentId()));

        verify(filePaymentConsentStoreClient).getConsent(eq(consentId), eq(TEST_API_CLIENT_ID));
        verifyConsentConsumed(consentId);
        assertThat(filePaymentsRepository.findById(responseData.getFilePaymentId())).isPresent();
    }

    @Test
    public void shouldGetDomesticPaymentById() {
        final String consentId = IntentType.PAYMENT_FILE_CONSENT.generateIntentId();
        final OBWriteFileConsent3 filePaymentConsent = createValidConsent();
        mockConsentStoreGetResponse(consentId, filePaymentConsent);

        final OBWriteFile2 filePayment = createPayment(consentId, filePaymentConsent);
        final HttpEntity<OBWriteFile2> filePaymentEntity = new HttpEntity<>(filePayment, HTTP_HEADERS);

        final ResponseEntity<OBWriteFileResponse3> createResponse = restTemplate.exchange(filePaymentsUrl(), HttpMethod.POST, filePaymentEntity, OBWriteFileResponse3.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        final String filePaymentId = createResponse.getBody().getData().getFilePaymentId();

        ResponseEntity<OBWriteFileResponse3> getResponse = restTemplate.exchange(filePaymentsIdUrl(filePaymentId),
                HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBWriteFileResponse3.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isEqualTo(createResponse.getBody());
    }

    @Test
    public void testIdempotentSubmission() {
        final String consentId = IntentType.PAYMENT_FILE_CONSENT.generateIntentId();
        final OBWriteFileConsent3 filePaymentConsent = createValidConsent();
        mockConsentStoreGetResponse(consentId, filePaymentConsent);

        final OBWriteFile2 filePayment = createPayment(consentId, filePaymentConsent);
        final HttpEntity<OBWriteFile2> filePaymentEntity = new HttpEntity<>(filePayment, HTTP_HEADERS);

        final ResponseEntity<OBWriteFileResponse3> firstResponse = restTemplate.exchange(filePaymentsUrl(), HttpMethod.POST, filePaymentEntity, OBWriteFileResponse3.class);
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Send the same request again (same payload + idempotencyKey)
        final ResponseEntity<OBWriteFileResponse3> secondResponse = restTemplate.exchange(filePaymentsUrl(), HttpMethod.POST, filePaymentEntity, OBWriteFileResponse3.class);
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(secondResponse.getBody()).isEqualTo(firstResponse.getBody());

        verify(filePaymentConsentStoreClient, times(2)).getConsent(eq(consentId), eq(TEST_API_CLIENT_ID));
        verifyConsentConsumed(consentId); // Verifies consume was only called once
    }

    @Test
    public void failsToCreateDomesticPaymentIfInitiationChanged() {
        final String consentId = IntentType.PAYMENT_FILE_CONSENT.generateIntentId();
        final OBWriteFileConsent3 filePaymentConsent = createValidConsent();
        mockConsentStoreGetResponse(consentId, filePaymentConsent);

        final OBWriteFileConsent3 secondConsent = createValidConsent();
        secondConsent.getData().getInitiation().setFileHash("changed the hash");
        final OBWriteFile2 filePayment = createPayment(consentId, secondConsent);
        final HttpEntity<OBWriteFile2> filePaymentEntity = new HttpEntity<>(filePayment, HTTP_HEADERS);

        ResponseEntity<OBErrorResponse1> errorResponse = restTemplate.postForEntity(filePaymentsUrl(), filePaymentEntity, OBErrorResponse1.class);
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorResponse.getBody().getMessage()).isEqualTo("An error happened when parsing the request arguments");
        assertThat(errorResponse.getBody().getErrors()).hasSize(1);
        assertThat(errorResponse.getBody().getErrors().get(0)).isEqualTo(OBRIErrorType.PAYMENT_INVALID_INITIATION.toOBError1("The Initiation field in the request does not match with the consent"));

        verify(filePaymentConsentStoreClient).getConsent(eq(consentId), eq(TEST_API_CLIENT_ID));
        verifyNoMoreInteractions(filePaymentConsentStoreClient);
    }

    @Test
    public void failsToCreateDomesticPaymentIfStatusNotAuthorised() {
        final String consentId = IntentType.PAYMENT_FILE_CONSENT.generateIntentId();
        final OBWriteFileConsent3 filePaymentConsent = createValidConsent();

        // Consent has Consumed state, validation must reject the payment
        mockConsentStoreGetResponse(consentId, filePaymentConsent, OBWriteFileConsentResponse4Data.StatusEnum.CONSUMED.toString());

        final OBWriteFile2 filePayment = createPayment(consentId, filePaymentConsent);
        final HttpEntity<OBWriteFile2> filePaymentEntity = new HttpEntity<>(filePayment, HTTP_HEADERS);

        ResponseEntity<OBErrorResponse1> errorResponse = restTemplate.postForEntity(filePaymentsUrl(), filePaymentEntity, OBErrorResponse1.class);
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorResponse.getBody().getMessage()).isEqualTo("An error happened when parsing the request arguments");
        assertThat(errorResponse.getBody().getErrors()).hasSize(1);
        assertThat(errorResponse.getBody().getErrors().get(0)).isEqualTo(OBRIErrorType.CONSENT_STATUS_NOT_AUTHORISED.toOBError1(StatusEnum.CONSUMED.toString()));

        verify(filePaymentConsentStoreClient).getConsent(eq(consentId), eq(TEST_API_CLIENT_ID));
        verifyNoMoreInteractions(filePaymentConsentStoreClient);
    }

    @Test
    public void shouldFailToGetReportFileGivenNotImplemented() {
        // Given
        String filePaymentId = "1234";
        String url = filePaymentsReportUrl(filePaymentId);

        // When
        ResponseEntity response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), Void.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
    }

    private String filePaymentsUrl() {
        return BASE_URL + port + FILE_PAYMENTS_URI;
    }

    private String filePaymentsIdUrl(String id) {
        return filePaymentsUrl() + "/" + id;
    }

    private String filePaymentsReportUrl(String id) {
        return filePaymentsIdUrl(id) + "/report-file";
    }

    private static OBWriteFile2 createPayment(String consentId, OBWriteFileConsent3 filePaymentConsent) {
        final OBWriteFile2 filePayment = new OBWriteFile2().data(
                new OBWriteFile2Data().consentId(consentId)
                                      .initiation(filePaymentConsent.getData().getInitiation()));
        return filePayment;
    }

    private void verifyConsentConsumed(String consentId) {
        // Verify that consumeConsent was called
        final ArgumentCaptor<ConsumePaymentConsentRequest> consumeReqCaptor = ArgumentCaptor.forClass(ConsumePaymentConsentRequest.class);
        verify(filePaymentConsentStoreClient).consumeConsent(consumeReqCaptor.capture());
        final ConsumePaymentConsentRequest consumeConsentReq = consumeReqCaptor.getValue();
        assertThat(consumeConsentReq.getApiClientId()).isEqualTo(TEST_API_CLIENT_ID);
        assertThat(consumeConsentReq.getConsentId()).isEqualTo(consentId);
    }

}