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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ErrorClient;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.services.PlatformClientService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.exceptions.InvalidConsentException;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

/**
 * Service to get the consent/intent retrieved from the cloud platform as OB object
 */
@Slf4j
@Service
@ComponentScan(basePackages = {"com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.services"})
public class ConsentService {

    private final PlatformClientService platformClientService;

    // Setting a custom-configured ObjectMapper Bean {@see RSApplicationConfiguration#Jackson2ObjectMapperBuilderCustomizer}
    private final ObjectMapper objectMapper;

    public ConsentService(PlatformClientService platformClientService, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.platformClientService = platformClientService;
    }

    /**
     *
     * @param authorization the authorization token as JWT to extract the 'aud' claim to identify the apiClient
     * @param intentId the consent/intent id
     * @return {@link JsonObject}
     */
    public JsonObject getIDMIntent(String authorization, String intentId) {
        try {
            return getOBConsentAsJsonObject(authorization, intentId, false);
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

    /**
     *
     * @param targetClass The class to deserialize the {@link JsonObject} passed
     * @param intent {@link JsonObject} to be deserialized
     * @param intentId the consent/intent id
     * @return Object deserialized
     * @param <T> The parametrized type of the class returned by this method.
     */
    public <T> T deserialize(Class<T> targetClass, JsonObject intent, String intentId) {
        try {
            // deserialize the consent
            log.debug("defaultObjectMapper: {}", objectMapper);
            log.debug("registeredModules: {}", objectMapper.getRegisteredModuleIds());
            log.debug("getDeserializationConfig: {}", objectMapper.getDeserializationConfig());
            log.debug("getSerializationConfig: {}", objectMapper.getSerializationConfig());
            return objectMapper.readValue(
                    intent.toString(),
                    targetClass
            );
        } catch (JsonProcessingException exception) {
            String errorMessage = String.format("%s %s", exception, exception.getMessage());
            log.error(errorMessage);
            ExceptionClient exceptionClient =
                    new ExceptionClient(
                            ErrorClient.builder()
                                    .errorType(ErrorType.REQUEST_BINDING_FAILED)
                                    .intentId(intentId)
                                    .apiClientId("")
                                    .build(), errorMessage);
//
            throw new InvalidConsentException(exceptionClient.getErrorClient().getErrorType(),
                    OBRIErrorType.REQUEST_BINDING_FAILED, errorMessage,
                    exceptionClient.getErrorClient().getApiClientId(),
                    exceptionClient.getErrorClient().getIntentId());
        }
    }

    /**
     *
     * @param targetClass The class to deserialize the {@link JsonObject} consent/intent retrieved from cloud platform
     * @param authorization the authorization token as JWT to extract the 'aud' claim to identify the apiClient
     * @param intentId the consent/intent id
     * @return OB Object
     * @param <T> The parametrized type of the class returned by this method.
     */
    public <T> T getOBIntentObject(Class<T> targetClass, String authorization, String intentId) {
        try {
            // deserialize the consent
            log.debug("defaultObjectMapper: {}", objectMapper);
            log.debug("registeredModules: {}", objectMapper.getRegisteredModuleIds());
            log.debug("getDeserializationConfig: {}", objectMapper.getDeserializationConfig());
            log.debug("getSerializationConfig: {}", objectMapper.getSerializationConfig());
            return objectMapper.readValue(
                    getOBConsentAsJsonObject(authorization, intentId, true).toString(),
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

    /**
     *
     * @param authorization the authorization token as JWT to extract the 'aud' claim to identify the apiClient
     * @param intentId the consent/intent id
     * @param underlyingOBIntentObject true to return only the underlying 'OBIntentObject', false to return all intent object
     * @return {@link JsonObject}
     * @throws ExceptionClient
     */
    private JsonObject getOBConsentAsJsonObject(String authorization, String intentId, boolean underlyingOBIntentObject) throws ExceptionClient {
        return platformClientService.getIntent(
                authorization.replace("Bearer", "").trim(),
                intentId,
                underlyingOBIntentObject
        );
    }

}
