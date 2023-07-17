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
package com.forgerock.sapi.gateway.rs.resource.store.api.resource.customerinfo;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.customerinfo.FRCustomerInfo;
import com.forgerock.sapi.gateway.rs.resource.store.api.testsupport.HttpHeadersTestDataFactory;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.customerinfo.FRCustomerInfoConverter;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.customerinfo.FRCustomerInfoEntity;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.customerinfo.FRCustomerInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

import static com.forgerock.sapi.gateway.rs.resource.store.api.testsupport.FRCustomerInfoTestHelper.aValidFRCustomerInfo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Spring Boot Test for {@link CustomerInfoResourceApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class CustomerInfoResourceApiControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String CUSTOMER_INFO_URI = "/resources/customerinfo/findByUserId";

    @LocalServerPort
    private int port;

    @Autowired
    private FRCustomerInfoRepository customerInfoRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterEach
    public void removeData() {
        customerInfoRepository.deleteAll();
    }

    @Test
    public void shouldGetCustomerInfo() {
        // Given
        String userId = UUID.randomUUID().toString();
        FRCustomerInfo customerInfo = aValidFRCustomerInfo(userId);
        FRCustomerInfoEntity customerInfoEntity = FRCustomerInfoConverter.dtoToEntity(customerInfo);
        customerInfoRepository.save(customerInfoEntity);

        // When
        ResponseEntity<FRCustomerInfo> response = restTemplate.exchange(
                findCustomerInfoByUserIdUri(userId),
                HttpMethod.GET,
                new HttpEntity<>(HttpHeadersTestDataFactory.requiredResourceHttpHeaders()),
                FRCustomerInfo.class);
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        FRCustomerInfo customerInfoResponse = response.getBody();
        validateResponse(customerInfoResponse, customerInfo);
    }

    @Test
    public void shouldThrownNotFound() {
        // Given
        String userId = UUID.randomUUID().toString();
        FRCustomerInfo customerInfo = aValidFRCustomerInfo(userId);
        FRCustomerInfoEntity customerInfoEntity = FRCustomerInfoConverter.dtoToEntity(customerInfo);
        customerInfoRepository.save(customerInfoEntity);

        // When
        ResponseEntity<FRCustomerInfo> response = restTemplate.exchange(
                findCustomerInfoByUserIdUri(UUID.randomUUID().toString()),
                HttpMethod.GET,
                new HttpEntity<>(HttpHeadersTestDataFactory.requiredResourceHttpHeaders()),
                FRCustomerInfo.class);
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private void validateResponse(FRCustomerInfo customerInfoResponse, FRCustomerInfo customerInfoExpected) {
        assertThat(customerInfoResponse.getUserID()).isEqualTo(customerInfoExpected.getUserID());
        assertThat(customerInfoResponse.getUserName()).isEqualTo(customerInfoExpected.getUserName());
        assertThat(customerInfoResponse.getTitle()).isEqualTo(customerInfoExpected.getTitle());
        assertThat(customerInfoResponse.getInitials()).isEqualTo(customerInfoExpected.getInitials());
        assertThat(customerInfoResponse.getGivenName()).isEqualTo(customerInfoExpected.getGivenName());
        assertThat(customerInfoResponse.getFamilyName()).isEqualTo(customerInfoExpected.getFamilyName());
        assertThat(customerInfoResponse.getBirthdate()).isEqualTo(customerInfoExpected.getBirthdate());
        assertThat(customerInfoResponse.getPhoneNumber()).isEqualTo(customerInfoExpected.getPhoneNumber());
        assertThat(customerInfoResponse.getEmail()).isEqualTo(customerInfoExpected.getEmail());
        assertThat(customerInfoResponse.getPartyId()).isEqualTo(customerInfoExpected.getPartyId());
        assertThat(customerInfoResponse.getAddress()).isEqualTo(customerInfoExpected.getAddress());
    }

    private String customerInfoUrl() {
        return BASE_URL + port + CUSTOMER_INFO_URI;
    }

    private URI findCustomerInfoByUserIdUri(String userId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(customerInfoUrl());
        builder.queryParam("userId", userId);
        return builder.build().encode().toUri();
    }

}
