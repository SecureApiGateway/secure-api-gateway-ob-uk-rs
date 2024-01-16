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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_1.domesticpayments;

import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments.DomesticPaymentSubmissionRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.PaymentSubmissionValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v3_1_1.domesticpayments.DomesticPaymentsApi;
import org.springframework.stereotype.Controller;

@Controller("DomesticPaymentsApiV3.1.1")
public class DomesticPaymentsApiController extends com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1.domesticpayments.DomesticPaymentsApiController implements DomesticPaymentsApi {

    public DomesticPaymentsApiController(DomesticPaymentSubmissionRepository paymentSubmissionRepository,
                                         PaymentSubmissionValidator paymentSubmissionValidator) {
        super(paymentSubmissionRepository, paymentSubmissionValidator);
    }
}
