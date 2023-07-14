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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_10.products;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_6.products.ProductsApiController;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.document.account.FRAccount;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.document.account.FRProduct;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.accounts.products.FRProductRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.account.consent.AccountResourceAccessService;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.org.openbanking.datamodel.account.OBReadProduct2;
import uk.org.openbanking.datamodel.account.OBReadProduct2DataProduct;

import java.util.UUID;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.testsupport.account.FRFinancialAccountTestDataFactory.aValidFRFinancialAccount;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.AccountResourceAccessServiceTestHelpers.createAuthorisedConsentAllPermissions;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.AccountResourceAccessServiceTestHelpers.mockAccountResourceAccessServiceResponse;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory.requiredAccountApiHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Spring Boot Test for {@link ProductsApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class ProductsApiControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String ACCOUNT_PRODUCT_URI = "/open-banking/v3.1.10/aisp/accounts/{AccountId}/product";
    private static final String PRODUCTS_URI = "/open-banking/v3.1.10/aisp/products";

    @LocalServerPort
    private int port;

    @Autowired
    private FRAccountRepository frAccountRepository;

    @Autowired
    private FRProductRepository frProductRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private AccountResourceAccessService accountResourceAccessService;

    private String accountId;

    @BeforeEach
    public void saveData() {
        FRFinancialAccount financialAccount = aValidFRFinancialAccount();
        FRAccount account = FRAccount.builder()
                .userID("AUserId")
                .account(financialAccount)
                .latestStatementId("5678")
                .build();
        frAccountRepository.save(account);
        accountId = account.getId();

        FRProduct product = FRProduct.builder()
                .accountId(accountId)
                .product(new OBReadProduct2DataProduct()
                        .productId(UUID.randomUUID().toString())
                        .accountId(accountId)
                        .productType(OBReadProduct2DataProduct.ProductTypeEnum.PERSONALCURRENTACCOUNT)
                )
                .build();
        frProductRepository.save(product);
    }

    @AfterEach
    public void removeData() {
        frAccountRepository.deleteAll();
        frProductRepository.deleteAll();
    }

    @Test
    public void shouldGetAccountProduct() {
        // Given
        String url = accountProductUrl(accountId);

        final AccountAccessConsent consent = createAuthorisedConsentAllPermissions(accountId);
        mockAccountResourceAccessServiceResponse(accountResourceAccessService, consent, accountId);

        // When
        ResponseEntity<OBReadProduct2> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(requiredAccountApiHeaders(consent.getId(), consent.getApiClientId())),
                OBReadProduct2.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBReadProduct2 returnedProduct = response.getBody();
        assertThat(returnedProduct).isNotNull();
        assertThat(returnedProduct.getData().getProduct().get(0).getAccountId()).isEqualTo(accountId);
        assertThat(response.getBody().getLinks().getSelf().toString()).isEqualTo(url);
    }

    @Test
    public void shouldGetProducts() {
        // Given
        String url = productsUrl();

        final AccountAccessConsent consent = createAuthorisedConsentAllPermissions(accountId);
        mockAccountResourceAccessServiceResponse(accountResourceAccessService, consent);

        // When
        ResponseEntity<OBReadProduct2> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(requiredAccountApiHeaders(consent.getId(), consent.getApiClientId())),
                OBReadProduct2.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBReadProduct2 returnedProduct = response.getBody();
        assertThat(returnedProduct).isNotNull();
        assertThat(returnedProduct.getData().getProduct().get(0).getAccountId()).isEqualTo(accountId);
        assertThat(response.getBody().getLinks().getSelf().toString()).isEqualTo(url);
    }

    private String accountProductUrl(String accountId) {
        String url = BASE_URL + port + ACCOUNT_PRODUCT_URI;
        return url.replace("{AccountId}", accountId);
    }

    private String productsUrl() {
        return BASE_URL + port + PRODUCTS_URI;
    }
}