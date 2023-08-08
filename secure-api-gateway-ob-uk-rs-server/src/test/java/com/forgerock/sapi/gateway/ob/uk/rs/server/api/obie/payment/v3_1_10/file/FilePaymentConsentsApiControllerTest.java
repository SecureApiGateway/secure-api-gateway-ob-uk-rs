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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.file;

import static com.forgerock.sapi.gateway.ob.uk.common.error.ErrorCode.OBRI_CONSENT_NOT_FOUND;
import static com.forgerock.sapi.gateway.ob.uk.common.error.ErrorCode.OBRI_PERMISSION_INVALID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteFileConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.DefaultPaymentFileType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.PaymentFileType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.util.payment.file.TestPaymentFileResources;
import com.forgerock.sapi.gateway.ob.uk.rs.server.util.payment.file.TestPaymentFileResources.TestPaymentFile;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException.ErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.file.v3_1_10.FilePaymentConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.file.v3_1_10.CreateFilePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.file.v3_1_10.FilePaymentConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.file.v3_1_10.FileUploadRequest;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.error.OBStandardErrorCodes1;
import uk.org.openbanking.datamodel.payment.OBWriteFileConsent3;
import uk.org.openbanking.datamodel.payment.OBWriteFileConsentResponse4;
import uk.org.openbanking.datamodel.payment.OBWriteFileConsentResponse4Data.StatusEnum;
import uk.org.openbanking.testsupport.payment.OBWriteFileConsentTestDataFactory;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class FilePaymentConsentsApiControllerTest {

    private static final String TEST_API_CLIENT_ID = "client_234093-49";

    private static final HttpHeaders HTTP_HEADERS = HttpHeadersTestDataFactory.requiredPaymentsHttpHeadersWithApiClientId(TEST_API_CLIENT_ID);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private FilePaymentConsentStoreClient consentStoreClient;

    private final TestPaymentFileResources testPaymentFileResources = TestPaymentFileResources.getInstance();

    private String controllerBaseUri() {
        return "http://localhost:" + port + "/open-banking/v3.1.10/pisp/file-payment-consents";
    }

    private String controllerGetConsentUri(String consentId) {
        return controllerBaseUri() + "/" + consentId;
    }

    private String controllerUploadFileUri(String consentId) {
        return controllerGetConsentUri(consentId) + "/file";
    }


    @Test
    public void testCreateConsent() {
        final String fileHash = "fileHash";
        final int numTransactions = 1;
        final BigDecimal controlSum = BigDecimal.ONE;
        final OBWriteFileConsent3 consentRequest = createValidConsentRequest(DefaultPaymentFileType.UK_OBIE_PAIN_001.getPaymentFileType(),
                                                                                fileHash, numTransactions, controlSum);
        final FilePaymentConsent consentStoreResponse = buildAwaitingUploadConsent(consentRequest);
        when(consentStoreClient.createConsent(any())).thenAnswer(invocation -> {
            final CreateFilePaymentConsentRequest createConsentArg = invocation.getArgument(0, CreateFilePaymentConsentRequest.class);
            assertThat(createConsentArg.getApiClientId()).isEqualTo(TEST_API_CLIENT_ID);
            assertThat(createConsentArg.getConsentRequest()).isEqualTo(FRWriteFileConsentConverter.toFRWriteFileConsent(consentRequest));
            assertThat(createConsentArg.getCharges()).isEmpty();
            assertThat(createConsentArg.getIdempotencyKey()).isEqualTo(HTTP_HEADERS.getFirst("x-idempotency-key"));

            return consentStoreResponse;
        });

        final HttpEntity<OBWriteFileConsent3> entity = new HttpEntity<>(consentRequest, HTTP_HEADERS);

        final ResponseEntity<OBWriteFileConsentResponse4> createResponse = restTemplate.exchange(controllerBaseUri(), HttpMethod.POST,
                entity, OBWriteFileConsentResponse4.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        final OBWriteFileConsentResponse4 consentResponse = createResponse.getBody();
        final String consentId = consentResponse.getData().getConsentId();
        assertThat(consentId).isEqualTo(consentStoreResponse.getId());
        assertThat(consentResponse.getData().getStatus()).isEqualTo(StatusEnum.AWAITINGUPLOAD);
        assertThat(consentResponse.getData().getInitiation()).isEqualTo(consentRequest.getData().getInitiation());
        assertThat(consentResponse.getData().getAuthorisation()).isEqualTo(consentRequest.getData().getAuthorisation());
        assertThat(consentResponse.getData().getScASupportData()).isEqualTo(consentRequest.getData().getScASupportData());
        assertThat(consentResponse.getData().getCreationDateTime()).isNotNull();
        assertThat(consentResponse.getData().getStatusUpdateDateTime()).isNotNull();
        final String selfLinkToConsent = consentResponse.getLinks().getSelf().toString();
        assertThat(selfLinkToConsent).isEqualTo(controllerGetConsentUri(consentId));

        // Get the consent and verify it matches the create response
        when(consentStoreClient.getConsent(eq(consentId), eq(TEST_API_CLIENT_ID))).thenReturn(consentStoreResponse);

        final ResponseEntity<OBWriteFileConsentResponse4> getConsentResponse = restTemplate.exchange(selfLinkToConsent,
                HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBWriteFileConsentResponse4.class);

        assertThat(getConsentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getConsentResponse.getBody()).isEqualTo(consentResponse);
    }

    @Test
    void testUploadFile() {
        final String consentId = IntentType.PAYMENT_FILE_CONSENT.generateIntentId();

        final TestPaymentFile paymentFile = testPaymentFileResources.getPaymentFile(TestPaymentFileResources.PAIN_001_001_08_FILE_PATH);

        final String idempotencyKey = UUID.randomUUID().toString();
        final HttpEntity<String> entity = new HttpEntity<>(paymentFile.getFileContent(), createHeadersForFileUpload(idempotencyKey, paymentFile.getFileType()));

        given(consentStoreClient.getConsent(eq(consentId), eq(TEST_API_CLIENT_ID))).willReturn(buildAwaitingUploadConsent(
                createValidConsentRequest(paymentFile.getFileType(), paymentFile.getFileHash(), paymentFile.getNumTransactions(), paymentFile.getControlSum())));

        final ResponseEntity<Void> createResponse = restTemplate.exchange(controllerUploadFileUri(consentId), HttpMethod.POST,
                entity, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        final ArgumentCaptor<FileUploadRequest> fileUploadRequestArgumentCaptor = ArgumentCaptor.forClass(FileUploadRequest.class);
        verify(consentStoreClient).uploadFile(fileUploadRequestArgumentCaptor.capture());
        final FileUploadRequest fileUploadRequest = fileUploadRequestArgumentCaptor.getValue();
        assertThat(fileUploadRequest.getConsentId()).isEqualTo(consentId);
        assertThat(fileUploadRequest.getFileContents()).isEqualTo(paymentFile.getFileContent());
        assertThat(fileUploadRequest.getFileUploadIdempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(fileUploadRequest.getApiClientId()).isEqualTo(TEST_API_CLIENT_ID);
    }

    @Test
    void testFailToUploadInvalidFile() {
        final String consentId = IntentType.PAYMENT_FILE_CONSENT.generateIntentId();

        final TestPaymentFile paymentFile = testPaymentFileResources.getPaymentFile(TestPaymentFileResources.PAIN_001_001_08_FILE_PATH);

        final String idempotencyKey = UUID.randomUUID().toString();
        final HttpEntity<String> entity = new HttpEntity<>(paymentFile.getFileContent(), createHeadersForFileUpload(idempotencyKey, paymentFile.getFileType()));

        // Num transactions in consent != num in file
        given(consentStoreClient.getConsent(eq(consentId), eq(TEST_API_CLIENT_ID))).willReturn(buildAwaitingUploadConsent(
                createValidConsentRequest(paymentFile.getFileType(), paymentFile.getFileHash(), 1000, paymentFile.getControlSum())));

        final ResponseEntity<OBErrorResponse1> fileUploadResponse = restTemplate.exchange(controllerUploadFileUri(consentId), HttpMethod.POST,
                entity, OBErrorResponse1.class);

        assertThat(fileUploadResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        final OBErrorResponse1 obErrorResponse = fileUploadResponse.getBody();
        assertThat(obErrorResponse.getErrors().size()).isEqualTo(1);
        assertThat(obErrorResponse.getCode()).isEqualTo("OBRI.Request.Invalid");
        final OBError1 obError1 = obErrorResponse.getErrors().get(0);
        assertThat(obError1.getErrorCode()).isEqualTo("OBRI.Request.Object.file.wrong.number.of.transactions");
        assertThat(obError1.getMessage()).isEqualTo("The file received contains 3 transactions but the file consent metadata indicated that we are expecting a file with 1000 transactions'");

    }

    @Test
    public void failsToCreateConsentIfRequestDoesNotPassJavaBeanValidation() {
        final OBWriteFileConsent3 emptyConsent = new OBWriteFileConsent3();

        final ResponseEntity<OBErrorResponse1> response = restTemplate.exchange(controllerBaseUri(), HttpMethod.POST,
                new HttpEntity<>(emptyConsent, HTTP_HEADERS), OBErrorResponse1.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        final List<OBError1> errors = response.getBody().getErrors();
        assertThat(errors).hasSize(1);
        final String fieldInvalidErrCode = OBStandardErrorCodes1.UK_OBIE_FIELD_INVALID.toString();
        assertThat(errors.get(0)).isEqualTo(
                new OBError1().errorCode(fieldInvalidErrCode).message("The field received is invalid. Reason 'must not be null'").path("data"));

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
    public void testGetFileForConsent() {
        final String consentId = IntentType.PAYMENT_FILE_CONSENT.generateIntentId();
        final FilePaymentConsent consent = new FilePaymentConsent();
        final PaymentFileType paymentFileType = DefaultPaymentFileType.UK_OBIE_PAIN_001.getPaymentFileType();
        consent.setRequestObj(FRWriteFileConsentConverter.toFRWriteFileConsent(createValidConsentRequest(paymentFileType,
                "dfsfsd", 1, BigDecimal.ONE)));
        final String expectedFileContent = "<xml>";
        consent.setFileContent(expectedFileContent);
        when(consentStoreClient.getConsent(eq(consentId), eq(TEST_API_CLIENT_ID))).thenReturn(consent);

        final ResponseEntity<String> fileContentResponse = restTemplate.exchange(controllerUploadFileUri(consentId),
                HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), String.class);

        assertThat(fileContentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fileContentResponse.getHeaders().getContentType()).isEqualTo(paymentFileType.getContentType());
        assertThat(fileContentResponse.getBody()).isEqualTo(expectedFileContent);
    }

    @Test
    public void failsToGetFileForConsentNoFileUploaded() {
        final String consentId = IntentType.PAYMENT_FILE_CONSENT.generateIntentId();
        when(consentStoreClient.getConsent(eq(consentId), eq(TEST_API_CLIENT_ID))).thenReturn(new FilePaymentConsent());

        final ResponseEntity<OBErrorResponse1> errorResponse = restTemplate.exchange(controllerUploadFileUri(consentId),
                HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBErrorResponse1.class);


        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(errorResponse.getBody().getErrors()).hasSize(1).singleElement().isEqualTo(OBRIErrorType.NO_FILE_FOR_CONSENT.toOBError1());
    }


    private static OBWriteFileConsent3 createValidConsentRequest(PaymentFileType paymentFileType, String fileHash,
                                                                    int numTransactions, BigDecimal controlSum) {
        final OBWriteFileConsent3 consentRequest = OBWriteFileConsentTestDataFactory.aValidOBWriteFileConsent3(
                paymentFileType.getFileType(), fileHash, String.valueOf(numTransactions), controlSum);
        consentRequest.getData().getInitiation().setRequestedExecutionDateTime(consentRequest.getData().getInitiation().getRequestedExecutionDateTime().withZone(DateTimeZone.UTC));
        consentRequest.getData().getAuthorisation().setCompletionDateTime(consentRequest.getData().getAuthorisation().getCompletionDateTime().withZone(DateTimeZone.UTC));

        return consentRequest;
    }

    public static FilePaymentConsent buildAwaitingUploadConsent(OBWriteFileConsent3 consentRequest) {
        final FilePaymentConsent consentStoreResponse = new FilePaymentConsent();
        consentStoreResponse.setId(IntentType.PAYMENT_FILE_CONSENT.generateIntentId());
        consentStoreResponse.setRequestObj(FRWriteFileConsentConverter.toFRWriteFileConsent(consentRequest));
        consentStoreResponse.setStatus(StatusEnum.AWAITINGUPLOAD.toString());
        consentStoreResponse.setCharges(List.of());
        final DateTime creationDateTime = DateTime.now();
        consentStoreResponse.setCreationDateTime(creationDateTime);
        consentStoreResponse.setStatusUpdateDateTime(creationDateTime);
        return consentStoreResponse;
    }

    private static HttpHeaders createHeadersForFileUpload(String idempotencyKey, PaymentFileType paymentFileType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(paymentFileType.getContentType());
        headers.setBearerAuth("dummyAuthToken");
        headers.add("x-fapi-interaction-id", UUID.randomUUID().toString());
        headers.add("x-idempotency-key", idempotencyKey);
        headers.add("x-api-client-id", TEST_API_CLIENT_ID);
        headers.add("x-jws-signature", "dummySig");
        return headers;
    }

}