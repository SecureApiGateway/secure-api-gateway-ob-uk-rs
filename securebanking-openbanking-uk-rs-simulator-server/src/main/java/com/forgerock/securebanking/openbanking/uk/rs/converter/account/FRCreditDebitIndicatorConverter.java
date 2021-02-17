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
package com.forgerock.securebanking.openbanking.uk.rs.converter.account;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRCreditDebitIndicator;
import uk.org.openbanking.datamodel.account.*;

public class FRCreditDebitIndicatorConverter {

    // FR to OB
    public static OBCreditDebitCode toOBCreditDebitCode(FRCreditDebitIndicator indicator) {
        return indicator == null ? null : OBCreditDebitCode.valueOf(indicator.name());
    }

    public static OBCreditDebitCode1 toOBCreditDebitCode1(FRCreditDebitIndicator indicator) {
        return indicator == null ? null : OBCreditDebitCode1.valueOf(indicator.name());
    }

    public static OBTransaction5.CreditDebitIndicatorEnum toOBTransaction5CreditDebitIndicatorEnum(FRCreditDebitIndicator indicator) {
        return indicator == null ? null : OBTransaction5.CreditDebitIndicatorEnum.valueOf(indicator.name());
    }

    public static OBStatementFee2.CreditDebitIndicatorEnum toOBStatementFee2CreditDebitIndicatorEnum(FRCreditDebitIndicator indicator) {
        return indicator == null ? null : OBStatementFee2.CreditDebitIndicatorEnum.valueOf(indicator.name());
    }

    public static OBStatementInterest2.CreditDebitIndicatorEnum toOBStatementInterest2CreditDebitIndicatorEnum(FRCreditDebitIndicator indicator) {
        return indicator == null ? null : OBStatementInterest2.CreditDebitIndicatorEnum.valueOf(indicator.name());
    }

    // OB to FR
    public static FRCreditDebitIndicator toFRCreditDebitIndicator(OBCreditDebitCode indicator) {
        return indicator == null ? null : FRCreditDebitIndicator.valueOf(indicator.name());
    }

    public static FRCreditDebitIndicator toFRCreditDebitIndicator(OBCreditDebitCode1 indicator) {
        return indicator == null ? null : FRCreditDebitIndicator.valueOf(indicator.name());
    }

    public static FRCreditDebitIndicator toFRCreditDebitIndicator(OBStatementFee2.CreditDebitIndicatorEnum indicator) {
        return indicator == null ? null : FRCreditDebitIndicator.valueOf(indicator.name());
    }

    public static FRCreditDebitIndicator toFRCreditDebitIndicator(OBStatementInterest2.CreditDebitIndicatorEnum indicator) {
        return indicator == null ? null : FRCreditDebitIndicator.valueOf(indicator.name());
    }

    public static FRCreditDebitIndicator toFRCreditDebitIndicator(OBTransaction5.CreditDebitIndicatorEnum indicator) {
        return indicator == null ? null : FRCreditDebitIndicator.valueOf(indicator.name());
    }
}
