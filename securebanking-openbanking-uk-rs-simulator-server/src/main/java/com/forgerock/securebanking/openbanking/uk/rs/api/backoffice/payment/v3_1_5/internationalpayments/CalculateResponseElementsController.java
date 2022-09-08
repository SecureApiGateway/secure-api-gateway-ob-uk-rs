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
package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.v3_1_5.internationalpayments;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorResponseException;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorResponseCategory;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType;
import com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.calculate.elements.v3_1_5.internationalpayments.CalculateResponseElements;
import com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.utils.PaymentConsentGeneral;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalConsent5;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalConsentResponse6;

import javax.servlet.http.HttpServletRequest;


@RestController("CalculateInternationalPaymentsResponseElements_v3.1.5")
@Slf4j
public class CalculateResponseElementsController implements CalculateResponseElements {
    public static final String VALIDATION_TEST_FAILURE_HEADER = "x-validation-test-failure";

    public CalculateResponseElementsController() {

    }

    @Override
    public OBWriteInternationalConsentResponse6 calculateElements(
            OBWriteInternationalConsent5 body,
            String intent,
            String xFapiFinancialId,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            HttpServletRequest request) throws OBErrorResponseException {
        try {
            return (OBWriteInternationalConsentResponse6) PaymentConsentGeneral.calculate(body, intent, xFapiFinancialId, request);
        } catch (UnsupportedOperationException | JsonProcessingException e) {
            String message = String.format("%s", e.getMessage());
            log.error(message);
            throw badRequestResponseException(message);
        }
    }

    private OBErrorResponseException badRequestResponseException(String message) {
        return new OBErrorResponseException(
                HttpStatus.BAD_REQUEST,
                OBRIErrorResponseCategory.REQUEST_INVALID,
                OBRIErrorType.DATA_INVALID_REQUEST
                        .toOBError1(message)
        );
    }
}
