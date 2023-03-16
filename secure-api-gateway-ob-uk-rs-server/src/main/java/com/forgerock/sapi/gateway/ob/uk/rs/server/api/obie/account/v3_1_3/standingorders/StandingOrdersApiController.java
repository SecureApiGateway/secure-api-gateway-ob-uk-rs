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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_3.standingorders;

import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v3_1_3.standingorders.StandingOrdersApi;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.account.OBExternalPermissions1Code;
import uk.org.openbanking.datamodel.account.OBReadStandingOrder5;

import java.util.List;

@Controller("StandingOrdersApiV3.1.3")
public class StandingOrdersApiController implements StandingOrdersApi {

    private final com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_2.standingorders.StandingOrdersApiController previousVersionController;

    public StandingOrdersApiController(
            @Qualifier("StandingOrdersApiV3.1.2") com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_2.standingorders.StandingOrdersApiController previousVersionController
    ) {
        this.previousVersionController = previousVersionController;
    }

    @Override
    public ResponseEntity<OBReadStandingOrder5> getAccountStandingOrders(String accountId,
                                                                         int page,
                                                                         String authorization,
                                                                         DateTime xFapiAuthDate,
                                                                         String xFapiCustomerIpAddress,
                                                                         String xFapiInteractionId,
                                                                         String xCustomerUserAgent,
                                                                         List<OBExternalPermissions1Code> permissions,
                                                                         String httpUrl) {
        return previousVersionController.getAccountStandingOrders(
                accountId,
                page,
                authorization,
                xFapiAuthDate,
                xFapiCustomerIpAddress,
                xFapiInteractionId,
                xCustomerUserAgent,
                permissions,
                httpUrl);
    }

    @Override
    public ResponseEntity<OBReadStandingOrder5> getStandingOrders(int page,
                                                                  String authorization,
                                                                  DateTime xFapiAuthDate,
                                                                  String xFapiCustomerIpAddress,
                                                                  String xFapiInteractionId,
                                                                  String xCustomerUserAgent,
                                                                  List<String> accountIds,
                                                                  List<OBExternalPermissions1Code> permissions,
                                                                  String httpUrl) {
        return previousVersionController.getStandingOrders(
                page,
                authorization,
                xFapiAuthDate,
                xFapiCustomerIpAddress,
                xFapiInteractionId,
                xCustomerUserAgent,
                accountIds,
                permissions,
                httpUrl);
    }
}
