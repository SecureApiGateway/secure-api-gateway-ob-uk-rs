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
package com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.utils.url;

import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ErrorClient;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ExceptionClient;
import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.requireNonNull;

@Slf4j
public class UrlContext {

    public static final String USER_ID = "@UserId@";

    public static String replaceParameterContextValue(
            String context,
            String parameter,
            String value
    ) throws ExceptionClient {
        try {
            requireNonNull(context, "(UrlContextUtil#replaceParameterContextValue) parameter 'context' cannot be null");
            requireNonNull(parameter, "(UrlContextUtil#replaceParameterContextValue) parameter 'parameter' cannot be null");
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

    public static String UrlUserQueryFilter(
            String context,
            String queryFilter
    ) throws ExceptionClient {
        try {
            requireNonNull(context, "(UrlContextUtil#replaceParameterContextWithFilter) parameter 'context' cannot be null");
            requireNonNull(queryFilter, "(UrlContextUtil#replaceParameterContextWithFilter) parameter 'queryFilter' cannot be null");
        } catch (NullPointerException exception) {
            throw new ExceptionClient(
                    ErrorClient.builder()
                            .errorType(ErrorType.PARAMETER_ERROR)
                            .build(),
                    exception.getMessage(),
                    exception
            );
        }
        String result = context.replace("/" + USER_ID, queryFilter);
        return result;
    }
}
