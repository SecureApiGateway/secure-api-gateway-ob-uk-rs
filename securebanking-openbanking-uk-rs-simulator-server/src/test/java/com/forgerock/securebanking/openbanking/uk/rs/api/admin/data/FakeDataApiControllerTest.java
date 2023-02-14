package com.forgerock.securebanking.openbanking.uk.rs.api.admin.data;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
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
