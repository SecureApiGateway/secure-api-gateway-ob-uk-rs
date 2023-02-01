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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.v3_1_10.vrp;
import com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.validation.services.DomesticVrpValidationService;
import com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.services.ConsentService;
import com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.simulations.vrp.PeriodicLimitBreachResponseSimulatorService;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.payments.DomesticVrpPaymentSubmissionRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;

@Controller("DomesticVrpsApiV3.1.10")
@Slf4j
public class DomesticVrpsApiController extends com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.v3_1_9.vrp.DomesticVrpsApiController implements DomesticVrpsApi {

    public DomesticVrpsApiController(
            DomesticVrpPaymentSubmissionRepository paymentSubmissionRepository,
            DomesticVrpValidationService domesticVrpValidationService,
            ConsentService consentService,
            PeriodicLimitBreachResponseSimulatorService limitBreachResponseSimulatorService
    ) {
        super(paymentSubmissionRepository, domesticVrpValidationService, consentService, limitBreachResponseSimulatorService);
    }
}
