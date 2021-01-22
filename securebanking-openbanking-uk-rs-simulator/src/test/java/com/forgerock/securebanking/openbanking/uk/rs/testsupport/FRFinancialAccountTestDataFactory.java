/**
 * Copyright © 2020 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.testsupport;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccountServicer;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRFinancialAccount;
import org.joda.time.DateTime;

import java.util.List;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRFinancialAccount.FRAccountStatusCode.ENABLED;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRFinancialAccount.FRAccountSubTypeCode.CURRENTACCOUNT;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRFinancialAccount.FRAccountTypeCode.PERSONAL;
import static com.forgerock.securebanking.openbanking.uk.rs.testsupport.FRAccountIdentifierTestDataFactory.aValidFRAccountIdentifier;

/**
 * Test data factory for {@link FRFinancialAccount}.
 */
public class FRFinancialAccountTestDataFactory {

    public static FRFinancialAccount aValidFRFinancialAccount() {
        return FRFinancialAccount.builder()
                .accountId("1234")
                .status(ENABLED)
                .statusUpdateDateTime(DateTime.now())
                .currency("GBP")
                .accountType(PERSONAL)
                .accountSubType(CURRENTACCOUNT)
                .description("A personal current account")
                .nickname("House Account")
                .openingDate(DateTime.now().minusDays(1))
                .maturityDate(null)
                .accounts(List.of(aValidFRAccountIdentifier()))
                .servicer(FRAccountServicer.builder()
                        .schemeName("UK.OBIE.SortCodeAccountNumber")
                        .identification("9876")
                        .build())
                .build();
    }

}
