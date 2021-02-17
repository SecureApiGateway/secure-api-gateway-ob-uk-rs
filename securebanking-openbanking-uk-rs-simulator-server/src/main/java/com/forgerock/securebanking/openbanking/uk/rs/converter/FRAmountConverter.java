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
package com.forgerock.securebanking.openbanking.uk.rs.converter;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRAmount;
import uk.org.openbanking.datamodel.account.*;
import uk.org.openbanking.datamodel.payment.OBActiveOrHistoricCurrencyAndAmount;
import uk.org.openbanking.datamodel.payment.*;

public class FRAmountConverter {

    // OB to FR
    public static FRAmount toFRAmount(OBActiveOrHistoricCurrencyAndAmount0 amount) {
        return FRModelMapper.map(amount, FRAmount.class);
    }

    public static FRAmount toFRAmount(OBActiveOrHistoricCurrencyAndAmount amount) {
        return FRModelMapper.map(amount, FRAmount.class);
    }

    public static FRAmount toFRAmount(OBActiveOrHistoricCurrencyAndAmount1 amount) {
        return FRModelMapper.map(amount, FRAmount.class);
    }

    public static FRAmount toFRAmount(OBActiveOrHistoricCurrencyAndAmount2 amount) {
        return FRModelMapper.map(amount, FRAmount.class);
    }

    public static FRAmount toFRAmount(OBActiveOrHistoricCurrencyAndAmount3 amount) {
        return FRModelMapper.map(amount, FRAmount.class);
    }

    public static FRAmount toFRAmount(OBActiveOrHistoricCurrencyAndAmount4 amount) {
        return FRModelMapper.map(amount, FRAmount.class);
    }

    public static FRAmount toFRAmount(OBActiveOrHistoricCurrencyAndAmount9 amount) {
        return FRModelMapper.map(amount, FRAmount.class);
    }

    public static FRAmount toFRAmount(OBActiveOrHistoricCurrencyAndAmount10 amount) {
        return FRModelMapper.map(amount, FRAmount.class);
    }

    public static FRAmount toFRAmount(OBActiveOrHistoricCurrencyAndAmount11 amount) {
        return FRModelMapper.map(amount, FRAmount.class);
    }

    public static FRAmount toFRAmount(OBDomestic2InstructedAmount amount) {
        return FRModelMapper.map(amount, FRAmount.class);
    }

    public static FRAmount toFRAmount(OBDomesticStandingOrder3FirstPaymentAmount amount) {
        return FRModelMapper.map(amount, FRAmount.class);
    }

    public static FRAmount toFRAmount(OBDomesticStandingOrder3FinalPaymentAmount amount) {
        return FRModelMapper.map(amount, FRAmount.class);
    }

    public static FRAmount toFRAmount(OBDomesticStandingOrder3RecurringPaymentAmount amount) {
        return FRModelMapper.map(amount, FRAmount.class);
    }

    public static FRAmount toFRAmount(OBWriteDomestic2DataInitiationInstructedAmount amount) {
        return FRModelMapper.map(amount, FRAmount.class);
    }

    public static FRAmount toFRAmount(OBWriteDomesticStandingOrder3DataInitiationFinalPaymentAmount amount) {
        return FRModelMapper.map(amount, FRAmount.class);
    }

    public static FRAmount toFRAmount(OBWriteDomesticStandingOrder3DataInitiationRecurringPaymentAmount amount) {
        return FRModelMapper.map(amount, FRAmount.class);
    }

    public static FRAmount toFRAmount(OBWriteDomesticStandingOrder3DataInitiationFirstPaymentAmount amount) {
        return FRModelMapper.map(amount, FRAmount.class);
    }

    public static FRAmount toFRAmount(uk.org.openbanking.datamodel.account.OBActiveOrHistoricCurrencyAndAmount amount) {
        return FRModelMapper.map(amount, FRAmount.class);
    }

    // FR to OB
    public static OBActiveOrHistoricCurrencyAndAmount toOBActiveOrHistoricCurrencyAndAmount(FRAmount amount) {
        return FRModelMapper.map(amount, OBActiveOrHistoricCurrencyAndAmount.class);
    }

    public static OBActiveOrHistoricCurrencyAndAmount2 toOBActiveOrHistoricCurrencyAndAmount2(FRAmount amount) {
        return FRModelMapper.map(amount, OBActiveOrHistoricCurrencyAndAmount2.class);
    }

    public static OBActiveOrHistoricCurrencyAndAmount3 toOBActiveOrHistoricCurrencyAndAmount3(FRAmount amount) {
        return FRModelMapper.map(amount, OBActiveOrHistoricCurrencyAndAmount3.class);
    }

    public static OBActiveOrHistoricCurrencyAndAmount4 toOBActiveOrHistoricCurrencyAndAmount4(FRAmount amount) {
        return FRModelMapper.map(amount, OBActiveOrHistoricCurrencyAndAmount4.class);
    }

    public static OBActiveOrHistoricCurrencyAndAmount9 toOBActiveOrHistoricCurrencyAndAmount9(FRAmount amount) {
        return FRModelMapper.map(amount, OBActiveOrHistoricCurrencyAndAmount9.class);
    }

    public static OBActiveOrHistoricCurrencyAndAmount10 toOBActiveOrHistoricCurrencyAndAmount10(FRAmount amount) {
        return FRModelMapper.map(amount, OBActiveOrHistoricCurrencyAndAmount10.class);
    }

    public static OBActiveOrHistoricCurrencyAndAmount11 toOBActiveOrHistoricCurrencyAndAmount11(FRAmount amount) {
        return FRModelMapper.map(amount, OBActiveOrHistoricCurrencyAndAmount11.class);
    }

    public static uk.org.openbanking.datamodel.account.OBActiveOrHistoricCurrencyAndAmount toAccountOBActiveOrHistoricCurrencyAndAmount(FRAmount amount) {
        return FRModelMapper.map(amount, uk.org.openbanking.datamodel.account.OBActiveOrHistoricCurrencyAndAmount.class);
    }

    public static OBActiveOrHistoricCurrencyAndAmount0 toOBActiveOrHistoricCurrencyAndAmount0(FRAmount amount) {
        return FRModelMapper.map(amount, OBActiveOrHistoricCurrencyAndAmount0.class);
    }

    public static OBActiveOrHistoricCurrencyAndAmount1 toOBActiveOrHistoricCurrencyAndAmount1(FRAmount amount) {
        return FRModelMapper.map(amount, OBActiveOrHistoricCurrencyAndAmount1.class);
    }

    public static OBWriteDomestic2DataInitiationInstructedAmount toOBWriteDomestic2DataInitiationInstructedAmount(FRAmount amount) {
        return FRModelMapper.map(amount, OBWriteDomestic2DataInitiationInstructedAmount.class);
    }

    public static OBDomestic2InstructedAmount toOBDomestic2InstructedAmount(FRAmount amount) {
        return FRModelMapper.map(amount, OBDomestic2InstructedAmount.class);
    }

    public static OBWriteDomesticStandingOrder3DataInitiationFinalPaymentAmount toOBWriteDomesticStandingOrder3DataInitiationFinalPaymentAmount(FRAmount amount) {
        return FRModelMapper.map(amount, OBWriteDomesticStandingOrder3DataInitiationFinalPaymentAmount.class);
    }

    public static OBWriteDomesticStandingOrder3DataInitiationRecurringPaymentAmount toOBWriteDomesticStandingOrder3DataInitiationRecurringPaymentAmount(FRAmount amount) {
        return FRModelMapper.map(amount, OBWriteDomesticStandingOrder3DataInitiationRecurringPaymentAmount.class);
    }

    public static OBWriteDomesticStandingOrder3DataInitiationFirstPaymentAmount toOBWriteDomesticStandingOrder3DataInitiationFirstPaymentAmount(FRAmount amount) {
        return FRModelMapper.map(amount, OBWriteDomesticStandingOrder3DataInitiationFirstPaymentAmount.class);
    }

    public static OBDomesticStandingOrder3FirstPaymentAmount toOBDomesticStandingOrder3FirstPaymentAmount(FRAmount amount) {
        return FRModelMapper.map(amount, OBDomesticStandingOrder3FirstPaymentAmount.class);
    }

    public static OBDomesticStandingOrder3RecurringPaymentAmount toOBDomesticStandingOrder3RecurringPaymentAmount(FRAmount amount) {
        return FRModelMapper.map(amount, OBDomesticStandingOrder3RecurringPaymentAmount.class);
    }

    public static OBDomesticStandingOrder3FinalPaymentAmount toOBDomesticStandingOrder3FinalPaymentAmount(FRAmount amount) {
        return FRModelMapper.map(amount, OBDomesticStandingOrder3FinalPaymentAmount.class);
    }
}
