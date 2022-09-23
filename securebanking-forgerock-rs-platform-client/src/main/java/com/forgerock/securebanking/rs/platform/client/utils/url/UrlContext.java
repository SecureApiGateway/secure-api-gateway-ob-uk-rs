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
package com.forgerock.securebanking.rs.platform.client.utils.url;

import com.forgerock.securebanking.openbanking.uk.common.api.meta.share.IntentType;
import com.forgerock.securebanking.rs.platform.client.exceptions.ErrorClient;
import com.forgerock.securebanking.rs.platform.client.exceptions.ErrorType;
import com.forgerock.securebanking.rs.platform.client.exceptions.ExceptionClient;
import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.requireNonNull;

@Slf4j
public class UrlContext {

    public static final String INTENT_ID = "@IntentId@";
    public static String replaceParameterContextIntentId(String context, String intentId) throws ExceptionClient {

        try {
            requireNonNull(context, "(UrlContextUtil#replaceParameterContextIntentId) parameter 'context' cannot be null");
            requireNonNull(intentId, "(UrlContextUtil#replaceParameterContextIntentId) parameter 'consentId' cannot be null");
        } catch (NullPointerException exception) {
            throw new ExceptionClient(
                    ErrorClient.builder()
                            .errorType(ErrorType.PARAMETER_ERROR)
                            .intentId(intentId)
                            .build(),
                    exception.getMessage(),
                    exception
            );
        }
        IntentType intentType = IntentType.identify(intentId);
        if (intentType == null) {
            String errorMessage = String.format("It has not been possible identify the intent type '%s' to replace the context.", intentId);
            log.error("(UrlContextUtil#replaceParameterContextIntentId) {}", errorMessage);
            throw new ExceptionClient(
                    ErrorClient.builder()
                            .errorType(ErrorType.UNKNOWN_INTENT_TYPE)
                            .intentId(intentId)
                            .build(),
                    errorMessage
            );
        }
        return context.replace(INTENT_ID, intentId);
    }

    public static String replaceParameterContextValue(String context, String parameter, String value) throws ExceptionClient {

        try {
            requireNonNull(context, "(UrlContextUtil#replaceParameterContextValue) parameter 'context' cannot be null");
            requireNonNull(value, "(UrlContextUtil#replaceParameterContextValue) parameter 'parameter' cannot be null");
            requireNonNull(value, "(UrlContextUtil#replaceParameterContextValue) parameter 'value' cannot be null");
        } catch (NullPointerException exception) {
            throw new ExceptionClient(
                    ErrorClient.builder()
                            .errorType(ErrorType.PARAMETER_ERROR)
                            .build(),
                    exception.getMessage(),
                    exception
            );
        }

        return context.replace(parameter, value);
    }
}
