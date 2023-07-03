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
package com.forgerock.sapi.gateway.ob.uk.rs.server.common.refund;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRReadRefundAccount;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRReadRefundAccount.NO;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRReadRefundAccount.YES;

public class FRReadRefundAccountFactory {

    /**
     * Creates an instance of {@link FRReadRefundAccount} from the provided 'x-read-refund-account' header value.
     *
     * @param xReadRefundAccount The 'x-read-refund-account' header value which should be set to 'Yes' or 'No', or
     *                           null if it hasn't been provided.
     * @return The corresponding {@link FRReadRefundAccount} value.
     */
    public static FRReadRefundAccount frReadRefundAccount(String xReadRefundAccount) {
        if (xReadRefundAccount != null && xReadRefundAccount.equals(YES.getValue())) {
            return YES;
        }
        return NO;
    }

    public static boolean frReadRefundAccountAsBoolean(String xReadRefundAccount) {
        if (xReadRefundAccount != null && xReadRefundAccount.equals(YES.getValue())) {
            return true;
        }
        return false;
    }
}
