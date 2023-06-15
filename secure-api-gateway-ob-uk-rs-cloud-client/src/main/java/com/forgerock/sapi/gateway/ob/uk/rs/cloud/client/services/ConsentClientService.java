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
package com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.services;

import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.configuration.ConsentRepoConfiguration;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ErrorClient;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.model.ClientRequest;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.utils.jwt.JwtUtil;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.utils.url.UrlContext;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * An Identity Cloud Platform client service to retrieve the intent from IDM
 */
@Slf4j
@Service
public class ConsentClientService {

    private static final String IDM_RESPOND_OB_INTENT_OBJECT_FIELD = "OBIntentObject";
    private final RestTemplate restTemplate;
    private final ConsentRepoConfiguration configurationProperties;

    public ConsentClientService(RestTemplate restTemplate, ConsentRepoConfiguration configurationProperties) {
        this.restTemplate = restTemplate;
        this.configurationProperties = configurationProperties;
    }

    /**
     *
     * @param jwtAuthorization bearer token to extract the clientId
     * @param intentId consentId to find
     * @return {@link JsonObject}
     * @throws ExceptionClient
     */
    public JsonObject getIntent(String jwtAuthorization, String intentId, boolean underlyingOBIntentObject) throws ExceptionClient {
        // get the apiClientId from audience claim ('aud')
        List<String> audiences = JwtUtil.getAudiences(jwtAuthorization);
        log.debug("Building client request object with apiClientId={} and intentId={}", audiences.get(0), intentId);
        ClientRequest clientRequest = ClientRequest.builder()
                .intentId(intentId)
                .apiClientId(audiences.get(0))
                .build();
        return getIntentAsJsonObject(clientRequest, underlyingOBIntentObject);
    }

    private JsonObject getIntentAsJsonObject(ClientRequest clientRequest, boolean underlyingOBIntentObject) throws ExceptionClient {
        String intentId = clientRequest.getIntentId();
        log.debug("=> The consent detailsRequest id: '{}'", intentId);
        String apiClientId = clientRequest.getApiClientId();
        log.debug("=> The client id: '{}'", apiClientId);
        // Validate intent
        validateIntent(clientRequest);
        // Process response from Identity cloud platform
        JsonObject jsonIntent = processCloudClientResponse(request(intentId, GET), clientRequest);
        // Verify the PISP/AISP is the same as the one that created the consent requested
        verifyIntentCreator(jsonIntent, clientRequest);
        return response(jsonIntent, underlyingOBIntentObject);
    }

    private void validateIntent(ClientRequest clientRequest) throws ExceptionClient {
        if (Objects.isNull(IntentType.identify(clientRequest.intentId))) {
            String message = String.format("Invalid type for intent ID: '%s'", clientRequest.intentId);
            log.error(message);
            throw new ExceptionClient(clientRequest, ErrorType.UNKNOWN_INTENT_TYPE, message);
        }
    }

    private JsonObject processCloudClientResponse(
            JsonObject jsonIntent,
            ClientRequest clientRequest
    ) throws ExceptionClient {
        if (Objects.isNull(jsonIntent)) {
            String errorMessage = String.format(
                    "The PISP/AISP '%s' is referencing a consent detailsRequest '%s' that doesn't exist",
                    clientRequest.apiClientId,
                    clientRequest.intentId);
            log.error(errorMessage);
            throw new ExceptionClient(clientRequest, ErrorType.NOT_FOUND, errorMessage);
        } else if (!jsonIntent.has(IDM_RESPOND_OB_INTENT_OBJECT_FIELD)) {
            throw new ExceptionClient(
                    clientRequest,
                    ErrorType.NOT_FOUND,
                    "Server responded with invalid consent response, missing OBIntentObject field"
            );
        }
        return jsonIntent;
    }

    private void verifyIntentCreator(
            JsonObject jsonIntent,
            ClientRequest clientRequest
    ) throws ExceptionClient {
        if (!clientRequest.apiClientId.equals(jsonIntent.get("oauth2ClientId").getAsString())) {
            String errorMessage = String.format(
                    "The PISP/AISP '%s' created the consent detailsRequest '%S' but it's PISP/AISP '%s' that is trying to get" +
                            " consent for it.",
                    jsonIntent.get("oauth2ClientId"),
                    clientRequest.intentId,
                    clientRequest.apiClientId
            );
            log.error(errorMessage);
            throw new ExceptionClient(clientRequest, ErrorType.INVALID_REQUEST, errorMessage);
        }
    }

    private JsonObject request(String intentId, HttpMethod httpMethod) throws ExceptionClient {
        String consentURL;
        IntentType intentType = IntentType.identify(intentId);
        if (intentType == null) {
            String errorMessage = String.format("It has not been possible identify the intent type '%s'.", intentId);
            log.error("(CloudPlatformClientService#request) {}", errorMessage);
            throw new ExceptionClient(
                    ErrorClient.builder()
                            .errorType(ErrorType.UNKNOWN_INTENT_TYPE)
                            .intentId(intentId)
                            .build(),
                    errorMessage
            );
        }
        consentURL = configurationProperties.getConsentRepoBaseUri() +
                UrlContext.replaceParameterContextIntentId(
                        configurationProperties.getContextsRepoConsent().get(httpMethod.name()),
                        intentId
                );

        log.debug("(CloudPlatformClientService#request) {} the consent from platform: {}", httpMethod.name(), consentURL);
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    consentURL,
                    httpMethod,
                    null,
                    String.class);
            log.debug("(CloudPlatformClientService#request) response entity: " + responseEntity);

            return Objects.nonNull(responseEntity) && Objects.nonNull(responseEntity.getBody()) ?
                    JsonParser.parseString(responseEntity.getBody()).getAsJsonObject() :
                    null;
        } catch (RestClientException e) {
            log.error(ErrorType.SERVER_ERROR.getDescription(), e);
            throw new ExceptionClient(
                    ErrorClient.builder()
                            .errorType(ErrorType.SERVER_ERROR)
                            .intentId(intentId)
                            .build(),
                    e.getMessage()
            );
        }
    }

    private JsonObject response(JsonObject jsonIntent, boolean underlyingOBIntentObject) {
        if (underlyingOBIntentObject) {
            return jsonIntent.getAsJsonObject(IDM_RESPOND_OB_INTENT_OBJECT_FIELD);
        }
        return jsonIntent.getAsJsonObject();
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        // TODO - add additional required headers
        return headers;
    }
}
