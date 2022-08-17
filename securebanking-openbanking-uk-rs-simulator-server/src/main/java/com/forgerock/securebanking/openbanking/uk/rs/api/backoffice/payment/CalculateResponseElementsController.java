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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.forgerock.securebanking.openbanking.uk.common.api.meta.obie.OBVersion;
import com.forgerock.securebanking.openbanking.uk.common.api.meta.share.IntentType;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorResponseException;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorResponseCategory;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType;
import com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.calculation.PaymentConsentResponseCalculation;
import com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.calculation.PaymentConsentResponseCalculationFactory;
import com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.utils.CustomObjectMapper;
import com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.validation.PaymentConsentValidation;
import com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.validation.PaymentConsentValidationFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.error.OBError1;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;


@Controller
@Slf4j
public class CalculateResponseElementsController implements CalculateResponseElements {
    public static final String VALIDATION_TEST_FAILURE_HEADER = "x-validation-test-failure";
    public static final String API_VERSION_DESCRIPTION = "Api version";
    public static final String INTENT_TYPE_DESCRIPTION = "Intent type";
    private final CustomObjectMapper customObjectMapper;

    public CalculateResponseElementsController() {
        customObjectMapper = CustomObjectMapper.getCustomObjectMapper();
    }

    @Override
    public ResponseEntity calculateElements(
            String body,
            String intent,
            String version,
            String xFapiFinancialId,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            HttpServletRequest request) throws OBErrorResponseException {
        try {
            OBVersion apiVersion = OBVersion.fromString(version);
            isNull(apiVersion, API_VERSION_DESCRIPTION);
            IntentType intentType = IntentType.identify(intent);
            isNull(intentType, INTENT_TYPE_DESCRIPTION);

            log.info("{}, Calculate elements for intent {} version {}", xFapiFinancialId, intentType, apiVersion.getCanonicalName());
            log.debug("{}, Consent request\n {}", xFapiFinancialId, body);

            PaymentConsentValidation validation = PaymentConsentValidationFactory.getValidationInstance(intent);
            Object consentRequest = customObjectMapper.getObjectMapper().readValue(body, validation.getRequestClass(apiVersion));
            validation.validate(consentRequest);

            if (haveErrorEvents(validation.getErrors(), xFapiFinancialId)) {
                throw badRequestResponseException(validation.getErrors());
            }

            log.debug("{}, Validation passed for intent {} version {}", xFapiFinancialId, intentType, apiVersion.getCanonicalName());

            PaymentConsentResponseCalculation calculation = PaymentConsentResponseCalculationFactory.getCalculationInstance(intent);
            Object consentResponseObject = customObjectMapper.getObjectMapper().readValue(body, calculation.getResponseClass(apiVersion));
            Object consentEntityResponse = calculation.calculate(consentRequest, consentResponseObject);

            if (haveErrorEvents(calculation.getErrors(), xFapiFinancialId)) {
                throw badRequestResponseException(calculation.getErrors());
            }

            log.debug("{}, Calculation done for intent {} version {}", xFapiFinancialId, intentType, apiVersion.getCanonicalName());
            log.debug("{}, Sending the response {}", xFapiFinancialId, customObjectMapper.getObjectMapper().writeValueAsString(consentEntityResponse));

            return ResponseEntity.ok(customObjectMapper.getObjectMapper().writeValueAsString(consentEntityResponse));
        } catch (UnsupportedOperationException | JsonProcessingException e) {
            String message = String.format("%s", e.getMessage());
            log.error(message);
            throw badRequestResponseException(message);
        }
    }

    private void isNull(Object object, String objectName) throws OBErrorResponseException {
        if (Objects.isNull(object)) {
            String message = String.format("It has not been possible to determine the value of '%s'", objectName);
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

    private boolean haveErrorEvents(List<OBError1> errors, String xFapiFinancialId) {
        if (!errors.isEmpty()) {
            log.error("{}, Errors {}", xFapiFinancialId, errors);
            return true;
        }
        return false;
    }

    private OBErrorResponseException badRequestResponseException(List<OBError1> errors) {
        return new OBErrorResponseException(
                HttpStatus.BAD_REQUEST,
                OBRIErrorResponseCategory.REQUEST_INVALID,
                errors
        );
    }
}
