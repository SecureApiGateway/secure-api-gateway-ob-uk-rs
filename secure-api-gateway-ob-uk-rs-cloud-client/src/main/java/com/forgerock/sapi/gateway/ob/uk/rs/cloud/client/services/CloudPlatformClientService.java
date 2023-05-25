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

import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.utils.url.UrlContext;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.configuration.ConsentRepoConfiguration;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ErrorClient;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.model.ClientRequest;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Specific implementation service to retrieve the Intent from the platform
 */
@Service
@Slf4j
@ComponentScan(basePackages = {"com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.configuration"})
class CloudPlatformClientService implements PlatformClient {

    private static final String IDM_RESPOND_OB_INTENT_OBJECT_FIELD = "OBIntentObject";
    private final RestTemplate restTemplate;
    private final ConsentRepoConfiguration configurationProperties;

    public CloudPlatformClientService(RestTemplate restTemplate, ConsentRepoConfiguration configurationProperties) {
        this.restTemplate = restTemplate;
        this.configurationProperties = configurationProperties;
    }

    @Override
    public JsonObject getIntentAsJsonObject(ClientRequest clientRequest, boolean underlyingOBIntentObject) throws ExceptionClient {
        String intentId = clientRequest.getIntentId();
        log.debug("=> The consent detailsRequest id: '{}'", intentId);
        String apiClientId = clientRequest.getApiClientId();
        log.debug("=> The client id: '{}'", apiClientId);

        JsonObject consentDetails = request(intentId, GET);
        String errorMessage;
        if (consentDetails == null) {
            errorMessage = String.format("The PISP/AISP '%s' is referencing a consent detailsRequest '%s' that doesn't exist", apiClientId, intentId);
            log.error(errorMessage);
            throw new ExceptionClient(clientRequest, ErrorType.NOT_FOUND, errorMessage);
        }

        // Verify the PISP/AISP is the same as the one that created this consent ^
        if (!apiClientId.equals(consentDetails.get("oauth2ClientId").getAsString())) {
            errorMessage = String.format("The PISP/AISP '%s' created the consent detailsRequest '%S' but it's PISP/AISP '%s' that is trying to get" +
                    " consent for it.", consentDetails.get("oauth2ClientId"), intentId, apiClientId);
            log.error(errorMessage);
            throw new ExceptionClient(clientRequest, ErrorType.INVALID_REQUEST, errorMessage);
        }

        if (!consentDetails.has(IDM_RESPOND_OB_INTENT_OBJECT_FIELD)) {
            throw new ExceptionClient(clientRequest, ErrorType.NOT_FOUND, "Server responded with invalid consent response, missing OBIntentObject field");
        }
        if(underlyingOBIntentObject) {
            return consentDetails.getAsJsonObject(IDM_RESPOND_OB_INTENT_OBJECT_FIELD);
        }
        return consentDetails.getAsJsonObject();
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

            return responseEntity != null && responseEntity.getBody() != null ? JsonParser.parseString(responseEntity.getBody()).getAsJsonObject() : null;
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

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        // TODO - add additional required headers
        return headers;
    }
}
