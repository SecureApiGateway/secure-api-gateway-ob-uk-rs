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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_3.balances;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v3_1_3.balances.BalancesApi;

import uk.org.openbanking.datamodel.account.OBReadBalance1;

@Controller("BalancesApiV3.1.3")
public class BalancesApiController implements BalancesApi {

    private final com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_2.balances.BalancesApiController previousVersionController;

    public BalancesApiController(@Qualifier("BalancesApiV3.1.2") com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_2.balances.BalancesApiController previousVersionController) {
        this.previousVersionController = previousVersionController;
    }

    @Override
    public ResponseEntity<OBReadBalance1> getAccountBalances(String accountId,
                                                             int page,
                                                             String authorization,
                                                             DateTime xFapiAuthDate,
                                                             String xFapiCustomerIpAddress,
                                                             String xFapiInteractionId,
                                                             String xCustomerUserAgent,
                                                             String consentId,
                                                             String apiClientId) throws OBErrorException {

        return previousVersionController.getAccountBalances(accountId,
                                                            page,
                                                            authorization,
                                                            xFapiAuthDate,
                                                            xFapiCustomerIpAddress,
                                                            xFapiInteractionId,
                                                            xCustomerUserAgent,
                                                            consentId,
                                                            apiClientId);
    }

    @Override
    public ResponseEntity<OBReadBalance1> getBalances(int page,
                                                      String authorization,
                                                      DateTime xFapiAuthDate,
                                                      String xFapiCustomerIpAddress,
                                                      String xFapiInteractionId,
                                                      String xCustomerUserAgent,
                                                      String consentId,
                                                      String apiClientId) throws OBErrorException {

        return previousVersionController.getBalances(page,
                                                     authorization,
                                                     xFapiAuthDate,
                                                     xFapiCustomerIpAddress,
                                                     xFapiInteractionId,
                                                     xCustomerUserAgent,
                                                     consentId,
                                                     apiClientId);
    }

}
