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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.event.v3_1_10.callbackurl;

import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.event.FRCallbackUrl;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.events.CallbackUrlsRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.org.openbanking.datamodel.event.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion.v3_0;
import static com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion.v3_1_10;
import static com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion.v3_1_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;

/**
 * A SpringBoot test for the {@link CallbackUrlsApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class CallbackUrlsApiControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String CALLBACK_URI = "/open-banking/v3.1.10/callback-urls";

    @LocalServerPort
    private int port;

    @Autowired
    private CallbackUrlsRepository callbackUrlsRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterEach
    void removeData() {
        callbackUrlsRepository.deleteAll();
    }

    @Test
    public void shouldCreateCallbackUrl() {
        // Given
        OBCallbackUrl1 obCallbackUrl1 = aValidOBCallbackUrl1();
        String tppId = UUID.randomUUID().toString();
        HttpEntity<OBCallbackUrl1> request = new HttpEntity<>(obCallbackUrl1, HttpHeadersTestDataFactory.requiredEventHttpHeaders(callbacksUrl(), tppId));

        // When
        ResponseEntity<OBCallbackUrlResponse1> response = restTemplate.postForEntity(callbacksUrl(), request, OBCallbackUrlResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        OBCallbackUrlResponseData1 responseData = response.getBody().getData();
        assertThat(responseData.getCallbackUrlId()).isNotNull();
        assertThat(responseData.getUrl()).isEqualTo(obCallbackUrl1.getData().getUrl());
        assertThat(responseData.getVersion()).isEqualTo(obCallbackUrl1.getData().getVersion());
        assertThat(response.getBody().getMeta()).isNotNull();
        assertThat(response.getBody().getLinks().getSelf()).isEqualTo(callbackIdUrl(responseData.getCallbackUrlId()));
    }

    @Test
    public void shouldReadCallBackUrls() {
        // Given
        OBCallbackUrl1 obCallbackUrl1 = aValidOBCallbackUrl1();
        String callbacksUrl = callbacksUrl();
        HttpHeaders headers = HttpHeadersTestDataFactory.requiredEventHttpHeaders(callbacksUrl, UUID.randomUUID().toString());
        HttpEntity<OBCallbackUrl1> request = new HttpEntity<>(obCallbackUrl1, headers);
        ResponseEntity<OBCallbackUrlResponse1> persistedCallback = restTemplate.postForEntity(callbacksUrl, request, OBCallbackUrlResponse1.class);

        // When
        ResponseEntity<OBCallbackUrlsResponse1> response = restTemplate.exchange(callbacksUrl, GET, new HttpEntity<>(headers), OBCallbackUrlsResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody().getData().getCallbackUrl()).isNotEmpty();
        OBCallbackUrlResponseData1 callbackUrlResponse = response.getBody().getData().getCallbackUrl().get(0);
        assertThat(callbackUrlResponse.getCallbackUrlId()).isEqualTo(persistedCallback.getBody().getData().getCallbackUrlId());
        assertThat(callbackUrlResponse.getUrl()).isEqualTo(obCallbackUrl1.getData().getUrl());
        assertThat(callbackUrlResponse.getVersion()).isEqualTo(obCallbackUrl1.getData().getVersion());
        assertThat(response.getBody().getMeta()).isNotNull();
        assertThat(response.getBody().getLinks().getSelf().toString()).isEqualTo(callbacksUrl);
    }

    @Test
    public void shouldUpdateCallbackUrl() {
        // Given
        OBCallbackUrl1 obCallbackUrl1 = aValidOBCallbackUrl1();
        HttpHeaders headers = HttpHeadersTestDataFactory.requiredEventHttpHeaders(callbacksUrl(), UUID.randomUUID().toString());
        HttpEntity<OBCallbackUrl1> createRequest = new HttpEntity<>(obCallbackUrl1, headers);
        ResponseEntity<OBCallbackUrlResponse1> persistedCallback = restTemplate.postForEntity(callbacksUrl(), createRequest, OBCallbackUrlResponse1.class);
        String updatedCallbackUrl = "http://updatedcallbackurl.com";
        obCallbackUrl1.getData().setUrl(updatedCallbackUrl);
        HttpEntity<OBCallbackUrl1> updateRequest = new HttpEntity<>(obCallbackUrl1, headers);
        URI url = callbackIdUrl(persistedCallback.getBody().getData().getCallbackUrlId());

        // When
        ResponseEntity<OBCallbackUrlResponse1> response = restTemplate.exchange(url, PUT, updateRequest, OBCallbackUrlResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody().getData().getUrl()).isEqualTo(updatedCallbackUrl);
        assertThat(response.getBody().getLinks().getSelf()).isEqualTo(url);
    }

    @Test
    public void shouldDeleteCallbackUrl() {
        // Given
        OBCallbackUrl1 obCallbackUrl1 = aValidOBCallbackUrl1();
        HttpHeaders headers = HttpHeadersTestDataFactory.requiredEventHttpHeaders(callbacksUrl(), UUID.randomUUID().toString());
        HttpEntity<OBCallbackUrl1> request = new HttpEntity<>(obCallbackUrl1, headers);
        ResponseEntity<OBCallbackUrlResponse1> persistedCallback = restTemplate.postForEntity(callbacksUrl(), request, OBCallbackUrlResponse1.class);
        URI deleteUrl = callbackIdUrl(persistedCallback.getBody().getData().getCallbackUrlId());

        // When
        restTemplate.exchange(deleteUrl, DELETE, new HttpEntity<>(headers), Void.class);

        // Then
        List<FRCallbackUrl> callbacks = callbackUrlsRepository.findAll();
        assertThat(callbacks).isEmpty();
    }

    private String callbacksUrl() {
        return BASE_URL + port + CALLBACK_URI;
    }

    private URI callbackIdUrl(String id) {
        return URI.create(callbacksUrl() + "/" + id);
    }

    private OBCallbackUrl1 aValidOBCallbackUrl1() {
        return new OBCallbackUrl1().data(new OBCallbackUrlData1()
                .url("http://callbackurl.com")
                .version(v3_1_10.getCanonicalName()));
    }
}