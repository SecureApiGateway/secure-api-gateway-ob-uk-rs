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
package com.forgerock.sapi.gateway.ob.uk.rs.server.service.idempotency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.org.openbanking.testsupport.v3.payment.OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomestic2;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRSubmissionStatus;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.payment.FRWriteDomesticConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomestic;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.payment.FRDomesticPaymentSubmission;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments.DomesticPaymentSubmissionRepository;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
class SinglePaymentForConsentIdempotentPaymentServiceTest {

    private final String apiClientId = "test-tpp-123";

    @Autowired
    private DomesticPaymentSubmissionRepository domesticPaymentRepository;

    private SinglePaymentForConsentIdempotentPaymentService<FRDomesticPaymentSubmission, FRWriteDomestic, DomesticPaymentSubmissionRepository> idempotentPaymentService;

    @BeforeEach
    void setup() {
        // Clear payment repo state
        domesticPaymentRepository.deleteAll();
        idempotentPaymentService = new SinglePaymentForConsentIdempotentPaymentService<>(domesticPaymentRepository);
    }

    public static void assertEquals(Object expectedPayment, Object actualPayment) {
        // Workaround an issue in Spring Mongo Auditing, the updated field is being updated when the insert fails
        // This looks like a bug in Spring, we do not use the field so ignore it for now
        assertThat(actualPayment).usingRecursiveComparison(RecursiveComparisonConfiguration.builder().withIgnoredFields("updated").build())
                .isEqualTo(expectedPayment);
    }

    private static FRDomesticPaymentSubmission createSubmission(String idempotencyKey, FRWriteDomestic obPayment) {
        return FRDomesticPaymentSubmission.builder()
                .obVersion(OBVersion.v3_1_10)
                .idempotencyKey(idempotencyKey)
                .status(FRSubmissionStatus.PENDING)
                .payment(obPayment)
                .created(new Date())
                .updated(new Date())
                .build();
    }

    @Test
    void testCreatePayment() throws Exception {
        final FRWriteDomestic obPayment = FRWriteDomesticConverter.toFRWriteDomestic(aValidOBWriteDomestic2());
        final String consentId = obPayment.getData().getConsentId();
        final String idempotencyKey = UUID.randomUUID().toString();

        final Optional<FRDomesticPaymentSubmission> expectNoPayment = idempotentPaymentService.findExistingPayment(obPayment, consentId, apiClientId, idempotencyKey);
        assertThat(expectNoPayment.isPresent()).isFalse();

        final FRDomesticPaymentSubmission paymentSubmission = createSubmission(idempotencyKey, obPayment);
        final FRDomesticPaymentSubmission persistedPaymentSubmission = idempotentPaymentService.savePayment(paymentSubmission);

        final Optional<FRDomesticPaymentSubmission> existingPayment = idempotentPaymentService.findExistingPayment(obPayment, consentId, apiClientId, idempotencyKey);
        assertThat(existingPayment.isPresent()).isTrue();
        assertEquals(existingPayment.get(), persistedPaymentSubmission);

        final FRDomesticPaymentSubmission secondPaymentSubmission = idempotentPaymentService.savePayment(paymentSubmission);
        assertEquals(persistedPaymentSubmission, secondPaymentSubmission);

        assertThat(domesticPaymentRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldFailToCreateIfPaymentExistsAndIdempotencyKeyIsDifferent() throws OBErrorException {
        final FRWriteDomestic obPayment = FRWriteDomesticConverter.toFRWriteDomestic(aValidOBWriteDomestic2());
        final FRDomesticPaymentSubmission paymentSubmission = createSubmission(UUID.randomUUID().toString(), obPayment);

        idempotentPaymentService.savePayment(paymentSubmission);

        // Change the idempotencyKey then try saving again
        paymentSubmission.setIdempotencyKey(UUID.randomUUID().toString());
        final OBErrorException obErrorException = assertThrows(OBErrorException.class, () -> idempotentPaymentService.savePayment(paymentSubmission));
        assertThat(obErrorException.getObriErrorType()).isEqualTo(OBRIErrorType.PAYMENT_SUBMISSION_ALREADY_EXISTS);
    }

    @Test
    void shouldFailToCreateIfPaymentExistsAndRequestBodyIsDifferent() throws OBErrorException {
        final FRWriteDomestic obPayment = FRWriteDomesticConverter.toFRWriteDomestic(aValidOBWriteDomestic2());
        final FRDomesticPaymentSubmission paymentSubmission = createSubmission(UUID.randomUUID().toString(), obPayment);

        idempotentPaymentService.savePayment(paymentSubmission);

        // Change the payment request data then try saving again
        obPayment.getData().getInitiation().setLocalInstrument("different value");
        final OBErrorException obErrorException = assertThrows(OBErrorException.class, () -> idempotentPaymentService.savePayment(paymentSubmission));
        assertThat(obErrorException.getObriErrorType()).isEqualTo(OBRIErrorType.IDEMPOTENCY_KEY_REQUEST_BODY_CHANGED);
    }
}