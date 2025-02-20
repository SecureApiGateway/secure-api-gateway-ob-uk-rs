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
package com.forgerock.sapi.gateway.rs.resource.store.api.admin.v3;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = {"rs.data.upload.limit.accounts=10", "rs.data.upload.limit.documents=1"})
@AutoConfigureWebClient(registerRestTemplate = true)
class FakeDataApiControllerTest {

    private static final String TEST_USERNAME = "psu4test";
    @Autowired
    private FakeDataApiController fakeDataApiController;

    @Test
    void testAccountIdGenerationForUserWithConfig() {
        final Supplier<String> accountIdSupplier = fakeDataApiController.createAccountIdSupplier(TEST_USERNAME);
        // verify we get the 3 preconfigured accountIds
        assertEquals("01233243245676", accountIdSupplier.get());
        assertEquals("01233254312390", accountIdSupplier.get());
        assertEquals("33441230187862", accountIdSupplier.get());

        // verify we get random accountIds after the preconfigured accountIds are exhausted
        verifySupplierGeneratesRandomIds(accountIdSupplier);
    }

    @Test
    void testAccountIdGenerationForUserWithoutConfig() {
        final Supplier<String> accountIdSupplier = fakeDataApiController.createAccountIdSupplier("newUser123");
        verifySupplierGeneratesRandomIds(accountIdSupplier);
    }

    private static void verifySupplierGeneratesRandomIds(Supplier<String> accountIdSupplier) {
        final Set<String> randomAccountIds = new HashSet<>();
        final int numRandomAccountIds = 10;
        for (int i = 0; i < numRandomAccountIds; i++) {
            final String randomAccountId = accountIdSupplier.get();
            assertEquals(14, randomAccountId.length());
            randomAccountIds.add(randomAccountId);
        }
        assertEquals(numRandomAccountIds, randomAccountIds.size());
    }
}
