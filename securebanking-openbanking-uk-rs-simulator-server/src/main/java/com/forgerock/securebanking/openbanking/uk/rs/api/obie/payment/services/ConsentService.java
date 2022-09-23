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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.services;

import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType;
import com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.utils.CustomObjectMapper;
import com.forgerock.securebanking.openbanking.uk.rs.exceptions.InvalidConsentException;
import com.forgerock.securebanking.rs.platform.client.exceptions.ErrorClient;
import com.forgerock.securebanking.rs.platform.client.exceptions.ErrorType;
import com.forgerock.securebanking.rs.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.rs.platform.client.services.PlatformClientService;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

/**
 * Service to retrieve Calculated elements from intent stored in IDM
 */
@Slf4j
@Service
@ComponentScan(basePackages = {"com.forgerock.securebanking.rs.platform.client.services"})
public class ConsentService {

    private final CustomObjectMapper customObjectMapper;
    private final PlatformClientService platformClientService;

    public ConsentService(PlatformClientService platformClientService) {
        this.customObjectMapper = CustomObjectMapper.getCustomObjectMapper();
        this.platformClientService = platformClientService;
    }

    public <T> T getOBConsent(Class<T> targetClass, String authorization, String intentId) {
        try {
            // deserialize the consent
            return customObjectMapper.getObjectMapper().readValue(
                    getOBConsentAsJsonObject(authorization, intentId).toString(),
                    targetClass
            );
        } catch (Exception exception) {
            String errorMessage = String.format("%s %s", exception, exception.getMessage());
            log.error(errorMessage);
            ExceptionClient exceptionClient = (exception instanceof ExceptionClient) ? (ExceptionClient) exception :
                    new ExceptionClient(
                            ErrorClient.builder()
                                    .errorType(ErrorType.REQUEST_BINDING_FAILED)
                                    .intentId(intentId)
                                    .apiClientId("")
                                    .build(), errorMessage);

            throw new InvalidConsentException(exceptionClient.getErrorClient().getErrorType(),
                    OBRIErrorType.REQUEST_BINDING_FAILED, errorMessage,
                    exceptionClient.getErrorClient().getApiClientId(),
                    exceptionClient.getErrorClient().getIntentId());
        }
    }

    private JsonObject getOBConsentAsJsonObject(String authorization, String intentId) throws ExceptionClient {
        return platformClientService.getIntent(
                authorization.replace("Bearer", "").trim(),
                intentId
        );
    }

}
