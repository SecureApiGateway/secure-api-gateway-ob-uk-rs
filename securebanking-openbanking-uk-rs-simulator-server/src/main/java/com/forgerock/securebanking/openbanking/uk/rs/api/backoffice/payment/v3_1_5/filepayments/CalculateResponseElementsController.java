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
package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.v3_1_5.filepayments;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorResponseException;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorResponseCategory;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType;
import com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.calculate.elements.v3_1_5.filepayments.CalculateResponseElements;
import com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.utils.PaymentConsentGeneral;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.org.openbanking.datamodel.payment.OBWriteFileConsent3;
import uk.org.openbanking.datamodel.payment.OBWriteFileConsentResponse4;

import javax.servlet.http.HttpServletRequest;

import static uk.org.openbanking.datamodel.payment.OBWriteFileConsentResponse4Data.StatusEnum.AWAITINGUPLOAD;


@RestController("CalculateFilePaymentsResponseElements_v3.1.5")
@Slf4j
public class CalculateResponseElementsController implements CalculateResponseElements {

    @Override
    public ResponseEntity<OBWriteFileConsentResponse4> calculateElements(
            OBWriteFileConsent3 body,
            String intent,
            String xFapiFinancialId,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            HttpServletRequest request) throws OBErrorResponseException {
        try {
            OBWriteFileConsentResponse4 response = PaymentConsentGeneral.calculate(
                    body, intent, xFapiFinancialId, request
            );
            response.getData().setStatus(AWAITINGUPLOAD);
            return ResponseEntity.status(HttpStatus.OK).body(response);
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