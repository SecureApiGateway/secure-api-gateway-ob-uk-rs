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
import com.forgerock.securebanking.rs.platform.client.exceptions.ErrorType;
import com.forgerock.securebanking.rs.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.rs.platform.client.model.ClientRequest;
import com.forgerock.securebanking.rs.platform.client.utils.jwt.JwtUtil;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PlatformClientService implements PlatformClient {

    private CloudPlatformClientService cloudPlatformClientService;

    public PlatformClientService(CloudPlatformClientService cloudPlatformClientService) {
        this.cloudPlatformClientService = cloudPlatformClientService;
    }

    @Override
    public JsonObject getIntentAsJsonObject(ClientRequest clientRequest) throws ExceptionClient {
        String intentId = clientRequest.getIntentId();
        log.debug("Retrieving the intent Id '{}", intentId);

        if (IntentType.identify(intentId) != null) {
            log.debug("Intent type: '{}' with ID '{}'", IntentType.identify(intentId), intentId);
            return cloudPlatformClientService.getIntentAsJsonObject(clientRequest);
        } else {
            String message = String.format("Invalid type for intent ID: '%s'", intentId);
            log.error(message);
            throw new ExceptionClient(clientRequest, ErrorType.UNKNOWN_INTENT_TYPE, message);
        }
    }

    public JsonObject getIntent(String jwtAuthorization, String intentId) throws ExceptionClient {
        // get the apiClientId from audience claim ('aud')
        List<String> audiences = JwtUtil.getAudiences(jwtAuthorization);
        log.debug("Building client request object with intentId={} and apiClientId={}", audiences.get(0), intentId);
        ClientRequest clientRequest = ClientRequest.builder()
                .intentId(intentId)
                .apiClientId(audiences.get(0))
                .build();
        return getIntentAsJsonObject(clientRequest);
    }
}