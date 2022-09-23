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
package com.forgerock.securebanking.rs.platform.client.services;

import com.forgerock.securebanking.openbanking.uk.common.api.meta.share.IntentType;
import com.forgerock.securebanking.rs.platform.client.configuration.ConfigurationPropertiesClient;
import com.forgerock.securebanking.rs.platform.client.exceptions.ErrorClient;
import com.forgerock.securebanking.rs.platform.client.exceptions.ErrorType;
import com.forgerock.securebanking.rs.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.rs.platform.client.model.ClientRequest;
import com.forgerock.securebanking.rs.platform.client.utils.url.UrlContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
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
@ComponentScan(basePackages = {"com.forgerock.securebanking.rs.platform.client.configuration"})
class CloudPlatformClientService implements PlatformClient {

    private final RestTemplate restTemplate;
    private final ConfigurationPropertiesClient configurationProperties;

    public CloudPlatformClientService(RestTemplate restTemplate, ConfigurationPropertiesClient configurationProperties) {
        this.restTemplate = restTemplate;
        this.configurationProperties = configurationProperties;
    }

    @Override
    public JsonObject getIntentAsJsonObject(ClientRequest clientRequest) throws ExceptionClient {
        String intentId = clientRequest.getIntentId();
        log.debug("=> The consent detailsRequest id: '{}'", intentId);
        String apiClientId = clientRequest.getApiClientId();
        log.debug("=> The client id: '{}'", apiClientId);

        JsonObject consentDetails = request(intentId, GET, null);
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

        return consentDetails;
    }

    private JsonObject request(String intentId, HttpMethod httpMethod, HttpEntity httpEntity) throws ExceptionClient {
        String consentURL;
        IntentType intentType = IntentType.identify(intentId);
        if (intentType == null) {
            String errorMessage = String.format("It has not been possible identify the intent type '%s'.", intentId);
            log.error("(ConsentService#request) {}", errorMessage);
            throw new ExceptionClient(
                    ErrorClient.builder()
                            .errorType(ErrorType.UNKNOWN_INTENT_TYPE)
                            .intentId(intentId)
                            .build(),
                    errorMessage
            );
        }
        consentURL = configurationProperties.getIgFqdnURIAsString() +
                UrlContext.replaceParameterContextIntentId(
                        configurationProperties.getContextsRepoConsent().get(httpMethod.name()),
                        intentId
                );

        log.debug("(ConsentService#request) {} the consent details from platform: {}", httpMethod.name(), consentURL);
        log.debug("Entity To {}: {}", httpMethod.name(), httpEntity != null ? httpEntity.getBody().toString() : "null");
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    consentURL,
                    httpMethod,
                    httpEntity,
                    String.class);
            log.debug("(ConsentService#request) response entity: " + responseEntity);

            return responseEntity != null && responseEntity.getBody() != null ? new JsonParser().parse(responseEntity.getBody()).getAsJsonObject() : null;
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
