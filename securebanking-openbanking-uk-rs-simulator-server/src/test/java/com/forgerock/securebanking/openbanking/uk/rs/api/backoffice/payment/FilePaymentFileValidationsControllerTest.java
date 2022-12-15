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

/*package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment;

import com.adelean.inject.resources.junit.jupiter.GivenTextResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import com.forgerock.securebanking.openbanking.uk.common.api.meta.obie.OBVersion;
import com.forgerock.securebanking.openbanking.uk.common.api.meta.share.IntentType;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorResponseCategory;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType;
import com.forgerock.securebanking.openbanking.uk.rs.common.filepayment.PaymentFile;
import com.forgerock.securebanking.openbanking.uk.rs.common.filepayment.PaymentFileFactory;
import com.forgerock.securebanking.openbanking.uk.rs.common.filepayment.PaymentFileType;
import com.forgerock.securebanking.openbanking.uk.rs.common.util.HashUtils;
import com.forgerock.securebanking.openbanking.uk.rs.testsupport.api.HttpHeadersTestDataFactory;
import com.forgerock.securebanking.rs.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.rs.platform.client.services.PlatformClientService;
import com.forgerock.securebanking.rs.platform.client.test.support.FilePaymentIntentTestModel;
import com.forgerock.securebanking.rs.platform.client.test.support.FilePaymentPlatformIntentTestFactory;
import com.google.gson.JsonObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;
import uk.org.openbanking.datamodel.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.payment.OBWriteFile2DataInitiation;
import uk.org.openbanking.datamodel.payment.OBWriteFileConsentResponse4;
import uk.org.openbanking.datamodel.payment.OBWriteFileConsentResponse4Data;
import uk.org.openbanking.testsupport.payment.OBWriteFileConsentTestDataFactory;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@TestWithResources
public class FilePaymentFileValidationsControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @GivenTextResource("payment/files/UK_OBIE_pain_001_001_08.xml")
    static String UK_OBIE_pain_001_001_08_Content;

    @GivenTextResource("payment/files/UK_OBIE_PaymentInitiation_3_1.json")
    static String UK_OBIE_PaymentInitiation_3_1_Content;

    @MockBean
    private PlatformClientService platformClientService;

    private static final String BASE_URL = "http://localhost:";

    private static final String REST_CONTEXT = "/backoffice/{0}/file-payment-consent/{1}/file/validate";

    private static final HttpHeaders HTTP_HEADERS = HttpHeadersTestDataFactory.requiredBackofficeHttpHeaders();

    private static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                Arguments.arguments(
                        PaymentFileType.UK_OBIE_PAIN_001,
                        PaymentFileType.UK_OBIE_PAYMENT_INITIATION_V3_1.getContentType() // wrong content type checks
                ),
                Arguments.arguments(
                        PaymentFileType.UK_OBIE_PAYMENT_INITIATION_V3_1,
                        PaymentFileType.UK_OBIE_PAIN_001.getContentType() // wrong content type checks
                )
        );
    }

    @ParameterizedTest
    @MethodSource("argumentsProvider")
    public void shouldValidateFileContent(PaymentFileType paymentFileType) throws ExceptionClient, OBErrorException {
        // Given
        String fileContent = getFileContent(paymentFileType);
        String intentId = IntentType.PAYMENT_FILE_CONSENT.generateIntentId();
        PaymentFile paymentFile = PaymentFileFactory.createPaymentFile(paymentFileType, fileContent);
        JsonObject jsonObject = FilePaymentPlatformIntentTestFactory.aValidFilePaymentPlatformIntent(
                FilePaymentIntentTestModel.builder()
                        .consentId(intentId)
                        .fileHash(HashUtils.computeSHA256FullHash(fileContent))
                        .fileType(paymentFileType.getFileType())
                        .fileReference("Test of " + paymentFileType.getFileType())
                        .controlSum(paymentFile.getControlSum())
                        .numberOfTransactions(String.valueOf(paymentFile.getNumberOfTransactions()))
                        .status(OBWriteFileConsentResponse4Data.StatusEnum.AWAITINGUPLOAD)
                        .build()
        );
        HTTP_HEADERS.setContentType(paymentFileType.getContentType());
        HTTP_HEADERS.setBearerAuth("token");
        HttpEntity<String> request = new HttpEntity<>(fileContent, HTTP_HEADERS);
        given(platformClientService.getIntent(anyString(), anyString(), anyBoolean())).willReturn(jsonObject);

        // When
        ResponseEntity<Void> response = restTemplate.exchange(
                getUri(OBVersion.v3_1_4.getCanonicalName(), intentId),
                HttpMethod.POST,
                request,
                Void.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @ParameterizedTest
    @MethodSource("argumentsProvider")
    public void shouldFailValidationFileContent(PaymentFileType paymentFileType) throws ExceptionClient, OBErrorException {
        // Given
        String wrongFileHash = UUID.randomUUID().toString();
        String fileContent = getFileContent(paymentFileType);
        String intentId = IntentType.PAYMENT_FILE_CONSENT.generateIntentId();
        PaymentFile paymentFile = PaymentFileFactory.createPaymentFile(paymentFileType, fileContent);
        OBWriteFileConsentResponse4 consentResponse4 = OBWriteFileConsentTestDataFactory.aValidOBWriteFileConsentResponse4*/
/**//*
(intentId);
        OBWriteFile2DataInitiation initiation = consentResponse4.getData().getInitiation();
        initiation.setFileHash(wrongFileHash);
        initiation.setControlSum(BigDecimal.ONE);
        initiation.setFileType(paymentFileType.getFileType());
        initiation.setNumberOfTransactions("99999");
        JsonObject jsonObject = FilePaymentPlatformIntentTestFactory.aValidFilePaymentPlatformIntent(
                FilePaymentIntentTestModel.builder()
                        .consentId(consentResponse4.getData().getConsentId())
                        .fileHash(wrongFileHash)
                        .fileType(paymentFileType.getFileType())
                        .fileReference("Test file")
                        .controlSum(paymentFile.getControlSum())
                        .numberOfTransactions(String.valueOf(paymentFile.getNumberOfTransactions()))
                        .status(OBWriteFileConsentResponse4Data.StatusEnum.AWAITINGUPLOAD)
                        .build()
        );
        HTTP_HEADERS.setContentType(paymentFileType.getContentType());
        HTTP_HEADERS.setBearerAuth("token");
        HttpEntity<String> request = new HttpEntity<>(fileContent, HTTP_HEADERS);
        given(platformClientService.getIntent(anyString(), anyString(), anyBoolean())).willReturn(jsonObject);

        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.exchange(
                getUri(OBVersion.v3_1_4.getCanonicalName(), intentId),
                HttpMethod.POST,
                request,
                OBErrorResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getId());
        assertThat(response.getBody().getMessage()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getDescription());
        assertThat(response.getBody().getErrors()).containsAnyElementsOf(
                List.of(
                        new OBErrorException(
                                OBRIErrorType.REQUEST_FILE_WRONG_NUMBER_OF_TRANSACTIONS,
                                paymentFile.getNumberOfTransactions(),
                                initiation.getNumberOfTransactions()
                        ).getOBError(),
                        new OBErrorException(
                                OBRIErrorType.REQUEST_FILE_INCORRECT_CONTROL_SUM,
                                paymentFile.getControlSum().toPlainString(),
                                initiation.getControlSum().toPlainString()
                        ).getOBError(),
                        new OBErrorException(
                                OBRIErrorType.REQUEST_FILE_INCORRECT_FILE_HASH,
                                HashUtils.computeSHA256FullHash(fileContent),
                                initiation.getFileHash()
                        ).getOBError()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("argumentsProvider")
    public void shouldFailContentType(PaymentFileType paymentFileType, MediaType wrongContentType) throws ExceptionClient, OBErrorException {
        // Given
        String wrongFileHash = UUID.randomUUID().toString();
        String fileContent = getFileContent(paymentFileType);
        String intentId = IntentType.PAYMENT_FILE_CONSENT.generateIntentId();
        PaymentFile paymentFile = PaymentFileFactory.createPaymentFile(paymentFileType, fileContent);
        OBWriteFileConsentResponse4 consentResponse4 = OBWriteFileConsentTestDataFactory.aValidOBWriteFileConsentResponse4(intentId);
        OBWriteFile2DataInitiation initiation = consentResponse4.getData().getInitiation();
        initiation.setFileHash(wrongFileHash);
        initiation.setControlSum(BigDecimal.ONE);
        initiation.setFileType(paymentFileType.getFileType());
        initiation.setNumberOfTransactions("99999");
        JsonObject jsonObject = FilePaymentPlatformIntentTestFactory.aValidFilePaymentPlatformIntent(
                FilePaymentIntentTestModel.builder()
                        .consentId(consentResponse4.getData().getConsentId())
                        .fileHash(wrongFileHash)
                        .fileType(paymentFileType.getFileType())
                        .fileReference("Test file")
                        .controlSum(paymentFile.getControlSum())
                        .numberOfTransactions(String.valueOf(paymentFile.getNumberOfTransactions()))
                        .status(OBWriteFileConsentResponse4Data.StatusEnum.AWAITINGUPLOAD)
                        .build()
        );
        HTTP_HEADERS.setContentType(wrongContentType);
        HTTP_HEADERS.setBearerAuth("token");
        HttpEntity<String> request = new HttpEntity<>(fileContent, HTTP_HEADERS);
        given(platformClientService.getIntent(anyString(), anyString(), anyBoolean())).willReturn(jsonObject);

        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.exchange(
                getUri(OBVersion.v3_1_4.getCanonicalName(), intentId),
                HttpMethod.POST,
                request,
                OBErrorResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getId());
        assertThat(response.getBody().getMessage()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getDescription());
        assertThat(response.getBody().getErrors()).containsExactly(
                new OBErrorException(
                        OBRIErrorType.REQUEST_MEDIA_TYPE_NOT_ACCEPTABLE, paymentFileType.getContentType()
                ).getOBError()
        );
    }

    private String getFileContent(PaymentFileType paymentFileType) {
        String content = "";
        switch (paymentFileType) {
            case UK_OBIE_PAIN_001 -> content = UK_OBIE_pain_001_001_08_Content;
            case UK_OBIE_PAYMENT_INITIATION_V3_1 -> content = UK_OBIE_PaymentInitiation_3_1_Content;
        }
        return content;
    }

    private URI getUri(String version, String intentId) {
        String restContext = java.text.MessageFormat.format(REST_CONTEXT, version, intentId);
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(BASE_URL + port + restContext);
        return builder.build().encode().toUri();
    }
}
*/
