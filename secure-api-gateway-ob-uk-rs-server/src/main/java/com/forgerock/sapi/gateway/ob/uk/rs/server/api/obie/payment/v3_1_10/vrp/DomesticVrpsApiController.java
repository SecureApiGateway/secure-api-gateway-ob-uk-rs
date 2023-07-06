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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.vrp;

import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.validation.DomesticVrpValidationService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.payments.DomesticVrpPaymentSubmissionRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.PaymentSubmissionValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.simulations.vrp.PeriodicLimitBreachResponseSimulatorService;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v3_1_10.vrp.DomesticVrpsApi;
import com.forgerock.sapi.gateway.rcs.conent.store.client.payment.vrp.v3_1_10.DomesticVRPConsentStoreClient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

@Controller("DomesticVrpsApiV3.1.10")
@Slf4j
public class DomesticVrpsApiController extends com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_9.vrp.DomesticVrpsApiController implements DomesticVrpsApi {

    public DomesticVrpsApiController(
            DomesticVrpPaymentSubmissionRepository paymentSubmissionRepository,
            DomesticVrpValidationService domesticVrpValidationService,
            DomesticVRPConsentStoreClient consentStoreClient,
            PeriodicLimitBreachResponseSimulatorService limitBreachResponseSimulatorService,
            PaymentSubmissionValidator paymentSubmissionValidator,
            FRAccountRepository frAccountRepository
    ) {
        super(
                paymentSubmissionRepository,
                domesticVrpValidationService,
                consentStoreClient,
                limitBreachResponseSimulatorService,
                paymentSubmissionValidator,
                frAccountRepository
        );
    }
}
