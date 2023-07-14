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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_5.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.AccountResourceAccessServiceTestHelpers;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;

class TransactionsApiControllerTest {

    private static final String TEST_ACC = "acc-1";


    @Test
    void checkPermissionsFails() {
        final TransactionsApiController controller = new TransactionsApiController(1, null, null, null);
        final AccountAccessConsent consent = AccountResourceAccessServiceTestHelpers.createAuthorisedConsent(List.of(FRExternalPermissionsCode.READBENEFICIARIESBASIC), TEST_ACC);
        final OBErrorException exception = assertThrows(OBErrorException.class, () -> controller.checkPermissions(consent));
        assertThat(exception.getObriErrorType()).isEqualTo(OBRIErrorType.PERMISSIONS_INVALID);
    }

    @Test
    void checkPermissionsPasses() throws OBErrorException {
        List<List<FRExternalPermissionsCode>> validPermissions = List.of(
                List.of(FRExternalPermissionsCode.READTRANSACTIONSCREDITS),
                List.of(FRExternalPermissionsCode.READTRANSACTIONSDETAIL),
                List.of(FRExternalPermissionsCode.READBENEFICIARIESBASIC, FRExternalPermissionsCode.READBALANCES, FRExternalPermissionsCode.READTRANSACTIONSDETAIL),
                List.of(FRExternalPermissionsCode.READBENEFICIARIESBASIC, FRExternalPermissionsCode.READBALANCES, FRExternalPermissionsCode.READTRANSACTIONSBASIC),
                Arrays.asList(FRExternalPermissionsCode.values())
        );

        final TransactionsApiController controller = new TransactionsApiController(1, null, null, null);

        for (List<FRExternalPermissionsCode> validPermission : validPermissions) {
            final AccountAccessConsent consent = AccountResourceAccessServiceTestHelpers.createAuthorisedConsent(validPermission);
            controller.checkPermissions(consent);
        }
    }
}