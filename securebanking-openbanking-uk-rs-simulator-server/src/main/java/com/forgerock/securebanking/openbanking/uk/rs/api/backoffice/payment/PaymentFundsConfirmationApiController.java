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
package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment;

import com.forgerock.securebanking.openbanking.uk.rs.service.balance.FundsAvailabilityService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.common.Meta;
import uk.org.openbanking.datamodel.payment.OBWriteFundsConfirmationResponse1;
import uk.org.openbanking.datamodel.payment.OBWriteFundsConfirmationResponse1Data;
import uk.org.openbanking.datamodel.payment.OBWriteFundsConfirmationResponse1DataFundsAvailableResult;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

import static com.forgerock.securebanking.openbanking.uk.rs.common.util.link.LinksHelper.createDomesticPaymentsConsentFundsConfirmationLink;

@Controller
@Slf4j
public class PaymentFundsConfirmationApiController implements PaymentFundsConfirmationApi {

    private final FundsAvailabilityService fundsAvailabilityService;

    public PaymentFundsConfirmationApiController(FundsAvailabilityService fundsAvailabilityService) {
        this.fundsAvailabilityService = fundsAvailabilityService;
    }

    @Override
    public ResponseEntity<OBWriteFundsConfirmationResponse1> getPaymentFundsConfirmation(
            String accountId,
            String amount,
            String version,
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            HttpServletRequest request,
            Principal principal) {

        // Check if funds are available on the account
        boolean areFundsAvailable = fundsAvailabilityService.isFundsAvailable(accountId, amount);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        new OBWriteFundsConfirmationResponse1()
                                .data(
                                        new OBWriteFundsConfirmationResponse1Data()
                                                .fundsAvailableResult(
                                                        new OBWriteFundsConfirmationResponse1DataFundsAvailableResult()
                                                                .fundsAvailable(areFundsAvailable)
                                                                .fundsAvailableDateTime(DateTime.now())
                                                )
                                                .supplementaryData(null)
                                )
                                .links(createDomesticPaymentsConsentFundsConfirmationLink(this.getClass(), version, accountId))
                                .meta(new Meta())
                );
    }
}
