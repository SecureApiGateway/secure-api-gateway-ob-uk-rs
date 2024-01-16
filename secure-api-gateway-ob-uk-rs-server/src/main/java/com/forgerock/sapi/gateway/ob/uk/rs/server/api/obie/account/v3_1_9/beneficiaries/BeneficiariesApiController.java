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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_9.beneficiaries;

import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.AccountDataInternalIdFilter;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.beneficiaries.FRBeneficiaryRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v3_1_9.beneficiaries.BeneficiariesApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.account.consent.AccountResourceAccessService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

@Controller("BeneficiariesApiV3.1.9")
public class BeneficiariesApiController extends com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_8.beneficiaries.BeneficiariesApiController implements BeneficiariesApi {

    public BeneficiariesApiController(@Value("${rs.page.default.beneficiaries.size:50}") int pageLimitBeneficiaries,
                                      FRBeneficiaryRepository frBeneficiaryRepository,
                                      AccountDataInternalIdFilter accountDataInternalIdFilter,
                                      AccountResourceAccessService accountResourceAccessService) {

        super(pageLimitBeneficiaries, frBeneficiaryRepository, accountDataInternalIdFilter, accountResourceAccessService);
    }
}
