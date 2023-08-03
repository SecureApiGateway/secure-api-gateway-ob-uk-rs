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
package com.forgerock.sapi.gateway.ob.uk.rs.server.service.idempotency;

import static com.forgerock.sapi.gateway.ob.uk.rs.server.service.idempotency.SinglePaymentForConsentIdempotentPaymentServiceTest.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Optional;
import java.util.UUID;

import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRSubmissionStatus;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.vrp.FRDomesticVrpConverters;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.vrp.FRDomesticVrpRequest;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.payment.FRDomesticVrpPaymentSubmission;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments.DomesticVrpPaymentSubmissionRepository;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import uk.org.openbanking.testsupport.vrp.OBDomesticVrpRequestTestDataFactory;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
class VRPIdempotentPaymentServiceTest {

    private final String apiClientId = "test-tpp-123";

    @Autowired
    private DomesticVrpPaymentSubmissionRepository vrpPaymentSubmissionRepository;

    private VRPIdempotentPaymentService idempotentPaymentService;

    @BeforeEach
    void setup() {
        // Clear payment repo state
        vrpPaymentSubmissionRepository.deleteAll();
        idempotentPaymentService = new VRPIdempotentPaymentService(vrpPaymentSubmissionRepository);
    }


    @Test
    void testCreatePayment() throws Exception {
        final FRDomesticVrpRequest obPayment = FRDomesticVrpConverters.toFRDomesticVRPRequest(OBDomesticVrpRequestTestDataFactory.aValidOBDomesticVRPRequest());
        final String consentId = obPayment.getData().getConsentId();
        final String idempotencyKey = UUID.randomUUID().toString();

        final Optional<FRDomesticVrpPaymentSubmission> expectNoPayment = idempotentPaymentService.findExistingPayment(obPayment, consentId, apiClientId, idempotencyKey);
        assertThat(expectNoPayment.isPresent()).isFalse();

        final FRDomesticVrpPaymentSubmission paymentSubmission = FRDomesticVrpPaymentSubmission.builder()
                .obVersion(OBVersion.v3_1_10)
                .apiClientId(apiClientId)
                .idempotencyKey(idempotencyKey)
                .idempotencyKeyExpiration(DateTime.now().plusMinutes(1))
                .status(FRSubmissionStatus.PENDING)
                .payment(obPayment)
                .created(DateTime.now())
                .updated(DateTime.now())
                .build();
        final FRDomesticVrpPaymentSubmission persistedPaymentSubmission = idempotentPaymentService.savePayment(paymentSubmission);

        final Optional<FRDomesticVrpPaymentSubmission> existingPayment = idempotentPaymentService.findExistingPayment(obPayment, consentId, apiClientId, idempotencyKey);
        assertThat(existingPayment.isPresent()).isTrue();
        assertEquals(existingPayment.get(), persistedPaymentSubmission);

        final FRDomesticVrpPaymentSubmission secondPaymentSubmission = idempotentPaymentService.savePayment(paymentSubmission);
        assertEquals(persistedPaymentSubmission, secondPaymentSubmission);

        assertThat(vrpPaymentSubmissionRepository.count()).isEqualTo(1);
    }

}