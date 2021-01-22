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
package com.forgerock.securebanking.openbanking.uk.rs.converter.payment;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRReadRefundAccount;
import uk.org.openbanking.datamodel.payment.*;

public class FRReadRefundAccountConverter {

    public static FRReadRefundAccount toFRReadRefundAccount(OBWriteDomesticConsent4Data.ReadRefundAccountEnum readRefundAccount) {
        return readRefundAccount == null ? null : FRReadRefundAccount.valueOf(readRefundAccount.name());
    }

    public static FRReadRefundAccount toFRReadRefundAccount(OBWriteDomesticScheduledConsent4Data.ReadRefundAccountEnum readRefundAccount) {
        return readRefundAccount == null ? null : FRReadRefundAccount.valueOf(readRefundAccount.name());
    }

    public static FRReadRefundAccount toFRReadRefundAccount(OBWriteDomesticStandingOrderConsent5Data.ReadRefundAccountEnum readRefundAccount) {
        return readRefundAccount == null ? null : FRReadRefundAccount.valueOf(readRefundAccount.name());
    }

    public static FRReadRefundAccount toFRReadRefundAccount(OBWriteInternationalConsent5Data.ReadRefundAccountEnum readRefundAccount) {
        return readRefundAccount == null ? null : FRReadRefundAccount.valueOf(readRefundAccount.name());
    }

    public static FRReadRefundAccount toFRReadRefundAccount(OBWriteInternationalScheduledConsent5Data.ReadRefundAccountEnum readRefundAccount) {
        return readRefundAccount == null ? null : FRReadRefundAccount.valueOf(readRefundAccount.name());
    }

    public static FRReadRefundAccount toFRReadRefundAccount(OBWriteInternationalStandingOrderConsent6Data.ReadRefundAccountEnum readRefundAccount) {
        return readRefundAccount == null ? null : FRReadRefundAccount.valueOf(readRefundAccount.name());
    }
}
