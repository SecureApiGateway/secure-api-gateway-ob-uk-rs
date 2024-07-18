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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_11.domesticstandingorders;

import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v3_1_11.domesticstandingorders.DomesticStandingOrdersApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.RefundAccountService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.PaymentSubmissionValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidationService;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteDomesticStandingOrder3Validator.OBWriteDomesticStandingOrder3ValidationContext;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.domesticstandingorder.v3_1_10.DomesticStandingOrderConsentStoreClient;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments.DomesticStandingOrderPaymentSubmissionRepository;

@Controller("DomesticStandingOrdersApiV3.1.11")
public class DomesticStandingOrdersApiController extends com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.domesticstandingorders.DomesticStandingOrdersApiController implements DomesticStandingOrdersApi {
    public DomesticStandingOrdersApiController(
            DomesticStandingOrderPaymentSubmissionRepository standingOrderPaymentSubmissionRepository,
            PaymentSubmissionValidator paymentSubmissionValidator,
            DomesticStandingOrderConsentStoreClient consentStoreClient,
            OBValidationService<OBWriteDomesticStandingOrder3ValidationContext> paymentValidator,
            RefundAccountService refundAccountService
    ) {
        super(
                standingOrderPaymentSubmissionRepository,
                paymentSubmissionValidator,
                consentStoreClient,
                paymentValidator,
                refundAccountService);
    }
}
