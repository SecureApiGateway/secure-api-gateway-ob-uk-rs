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

import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.configuration.CloudClientConfiguration;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ErrorClient;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.model.User;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.utils.url.UrlContext;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBHeaders;
import com.forgerock.sapi.gateway.uk.common.shared.fapi.FapiInteractionIdContext;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpMethod.GET;

/**
 * Specific service to retrieve the user data from the platform
 */
@Service
@Slf4j
public class UserClientService {
    private final RestTemplate restTemplate;
    private final CloudClientConfiguration cloudClientConfiguration;

    public UserClientService(RestTemplate restTemplate, CloudClientConfiguration cloudClientConfiguration) {
        this.restTemplate = restTemplate;
        this.cloudClientConfiguration = cloudClientConfiguration;
    }

    public User getUserByName(String userName) throws ExceptionClient {
        paramValidation(userName, "(UserClientService#getUserByName) the parameter 'userName' cannot be null");
        User user = request(userName, GET);
        if (Objects.isNull(user) || !user.getAccountStatus().equals("active")) {
            String message = String.format("User with userName '%s' not found.", userName);
            log.error(message);
            throw new ExceptionClient(
                    ErrorClient.builder()
                            .errorType(ErrorType.NOT_FOUND)
                            .reason(message)
                            .userName(userName)
                            .build(),
                    message
            );
        }
        return user;
    }

    private User request(String userName, HttpMethod httpMethod) throws ExceptionClient {
        String userFilter = "?_queryFilter=userName+eq+\"" + userName + "\"";
        String userFilterURL = cloudClientConfiguration.getBaseUri() +
                UrlContext.UrlUserQueryFilter(
                        cloudClientConfiguration.getContextsUser().get(httpMethod.name()),
                        userFilter);

        log.debug("(UserServiceClient#request) request the user details from platform: {}", userFilterURL);
        try {
            ResponseEntity<User> responseEntity = restTemplate.exchange(
                    userFilterURL,
                    GET,
                    createRequestEntity(),
                    User.class);

            return (responseEntity.getStatusCode() != HttpStatus.OK) ?
                    null :
                    responseEntity != null ? responseEntity.getBody() : null;
        } catch (HttpClientErrorException exception) {
            throw new ExceptionClient(
                    ErrorClient.builder()
                            .errorType(ErrorType.NOT_FOUND)
                            .reason(exception.getMessage())
                            .userName(userName)
                            .build(),
                    exception.getMessage()
            );
        }
    }

    private void paramValidation(String parameter, String message) throws ExceptionClient {
        try {
            requireNonNull(parameter, message);
        } catch (NullPointerException exception) {
            throw new ExceptionClient(
                    ErrorClient.builder()
                            .errorType(ErrorType.PARAMETER_ERROR)
                            .build(),
                    exception.getMessage(),
                    exception
            );
        }
    }

    protected HttpEntity<?> createRequestEntity() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(OBHeaders.X_FAPI_INTERACTION_ID, FapiInteractionIdContext.getFapiInteractionId()
                                                                                 .orElseGet(() -> UUID.randomUUID().toString()));
        return new HttpEntity<>(httpHeaders);
    }
}
