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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.funds.v3_1_4;

import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.funds.v3_1_4.FundsConfirmationsApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;

@Controller("FundsConfirmationsApiV3.1.4")
public class FundsConfirmationsApiController extends com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.funds.v3_1_3.FundsConfirmationsApiController implements FundsConfirmationsApi {

    public FundsConfirmationsApiController(@Qualifier("FundsConfirmationsApiV3.1.2") com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.funds.v3_1_2.FundsConfirmationsApiController baseController) {
        super(baseController);
    }
}
