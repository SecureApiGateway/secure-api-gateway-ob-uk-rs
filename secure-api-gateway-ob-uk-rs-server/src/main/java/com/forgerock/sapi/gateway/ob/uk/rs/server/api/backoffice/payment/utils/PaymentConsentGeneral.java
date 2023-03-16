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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.backoffice.payment.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.backoffice.payment.calculation.PaymentConsentResponseCalculation;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.backoffice.payment.calculation.PaymentConsentResponseCalculationFactory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.backoffice.payment.validation.PaymentConsentValidation;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.backoffice.payment.validation.PaymentConsentValidationFactory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.ApiVersionUtils;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import uk.org.openbanking.datamodel.error.OBError1;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

@Slf4j
public class PaymentConsentGeneral {
    public static final String API_VERSION_DESCRIPTION = "Api version";
    public static final String INTENT_TYPE_DESCRIPTION = "Intent type";
    private static final CustomObjectMapper customObjectMapper = CustomObjectMapper.getCustomObjectMapper();

    /**
     *
     * @param consentRequest OB consent request object
     * @param intent intent Id prefix (ex. 'PDC_') to identify the intent type
     * @param request http request object to obtain the api version
     * @return OB consent object response
     * @param <T> parametrized type for an instance of OB consent request object
     * @param <R> parametrized type for an instance of OB consent response object
     * @throws OBErrorResponseException
     * @throws JsonProcessingException
     */
    public static <T, R> R calculate(T consentRequest, String intent, HttpServletRequest request) throws OBErrorResponseException, JsonProcessingException {

        OBVersion apiVersion = ApiVersionUtils.getOBVersion(request.getRequestURI());
        isNull(apiVersion, API_VERSION_DESCRIPTION);
        IntentType intentType = IntentType.identify(intent);
        isNull(intentType, INTENT_TYPE_DESCRIPTION);

        log.info("Calculate elements for intent {} version {}", intentType, apiVersion.getCanonicalName());
        log.debug("Consent request\n {}", customObjectMapper.getObjectMapper().writeValueAsString(consentRequest));
        PaymentConsentValidation validation = PaymentConsentValidationFactory.getValidationInstance(intent);
        validation.clearErrors().validate(consentRequest);

        if (haveErrorEvents(validation.getErrors())) {
            throw badRequestResponseException(validation.getErrors());
        }

        log.debug("Validation passed for intent {} version {}", intentType, apiVersion.getCanonicalName());

        PaymentConsentResponseCalculation calculation = PaymentConsentResponseCalculationFactory.getCalculationInstance(intent);
        Object consentResponseObject = customObjectMapper.getObjectMapper().readValue(
                customObjectMapper.getObjectMapper().writeValueAsString(consentRequest),
                calculation.getResponseClass(apiVersion)
        );
        Object consentEntityResponse = calculation.clearErrors().calculate(consentRequest, consentResponseObject);

        if (haveErrorEvents(calculation.getErrors())) {
            throw badRequestResponseException(calculation.getErrors());
        }

        log.debug("Calculation done for intent {} version {}", intentType, apiVersion.getCanonicalName());
        log.debug("Sending the response {}", customObjectMapper.getObjectMapper().writeValueAsString(consentEntityResponse));

        return (R) consentEntityResponse;
    }

    private static boolean haveErrorEvents(List<OBError1> errors) {
        if (!errors.isEmpty()) {
            log.error("Errors {}", errors);
            return true;
        }
        return false;
    }

    private static OBErrorResponseException badRequestResponseException(List<OBError1> errors) {
        return new OBErrorResponseException(
                HttpStatus.BAD_REQUEST,
                OBRIErrorResponseCategory.REQUEST_INVALID,
                errors
        );
    }

    private static OBErrorResponseException badRequestResponseException(String message) {
        return new OBErrorResponseException(
                HttpStatus.BAD_REQUEST,
                OBRIErrorResponseCategory.REQUEST_INVALID,
                OBRIErrorType.DATA_INVALID_REQUEST
                        .toOBError1(message)
        );
    }

    private static void isNull(Object object, String objectName) throws OBErrorResponseException {
        if (Objects.isNull(object)) {
            String message = String.format("It has not been possible to determine the value of '%s'", objectName);
            log.error(message);
            throw badRequestResponseException(message);
        }
    }
}
