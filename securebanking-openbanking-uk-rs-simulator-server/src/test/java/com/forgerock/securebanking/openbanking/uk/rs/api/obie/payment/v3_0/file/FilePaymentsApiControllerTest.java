/**
 * Copyright © 2020 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.v3_0.file;

import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.payments.FilePaymentSubmissionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static com.forgerock.securebanking.openbanking.uk.rs.testsupport.api.HttpHeadersTestDataFactory.requiredPaymentHttpHeaders;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * A SpringBoot test for the {@link FilePaymentsApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class FilePaymentsApiControllerTest {

    private static final HttpHeaders HTTP_HEADERS = requiredPaymentHttpHeaders();
    private static final String BASE_URL = "http://localhost:";
    private static final String FILE_PAYMENTS_URI = "/open-banking/v3.0/pisp/file-payments";

    @LocalServerPort
    private int port;

    @Autowired
    private FilePaymentSubmissionRepository filePaymentsRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterEach
    void removeData() {
        filePaymentsRepository.deleteAll();
    }

    // TODO - implement test methods

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

}