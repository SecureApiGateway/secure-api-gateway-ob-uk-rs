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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.funds.v3_1_8;

import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.funds.v3_1_8.FundsConfirmationsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.balance.FundsAvailabilityService;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidationService;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.funds.FundsConfirmationValidator;
import com.forgerock.sapi.gateway.rcs.consent.store.client.funds.v3_1_10.FundsConfirmationConsentStoreClient;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.funds.FundsConfirmationRepository;

@Controller("FundsConfirmationsApiV3.1.8")
public class FundsConfirmationsApiController extends com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.funds.v3_1_7.FundsConfirmationsApiController implements FundsConfirmationsApi {

    public FundsConfirmationsApiController(
            FundsConfirmationRepository fundsConfirmationRepository,
            FundsAvailabilityService fundsAvailabilityService,
            FRAccountRepository accountRepository,
            OBValidationService<FundsConfirmationValidator.FundsConfirmationValidationContext> fundsConfirmationValidator,
            FundsConfirmationConsentStoreClient consentStoreClient
    ) {
        super(fundsConfirmationRepository, fundsAvailabilityService, accountRepository, fundsConfirmationValidator, consentStoreClient);
    }
}
