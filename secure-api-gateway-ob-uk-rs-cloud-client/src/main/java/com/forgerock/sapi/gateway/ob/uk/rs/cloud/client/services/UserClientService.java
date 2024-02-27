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
package com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.services;

import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.configuration.CloudClientConfiguration;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ErrorClient;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.model.User;
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
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
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

    /**
     * Template of the URI used to query for a user by username.
     * <p>
     * The username URI variable needs to be replaced with the actual user to query for.
     */
    private final UriComponents queryByUsernameUriTemplate;

    public UserClientService(RestTemplate restTemplate, CloudClientConfiguration cloudClientConfiguration) {
        this.restTemplate = restTemplate;
        this.queryByUsernameUriTemplate = UriComponentsBuilder.fromHttpUrl(cloudClientConfiguration.getBaseUri())
                                                              .path("/repo/users")
                                                              .queryParam("_queryFilter","userName+eq+\"{username}\"")
                                                              .build();
    }

    public User getUserByName(String userName) throws ExceptionClient {
        paramValidation(userName, "(UserClientService#getUserByName) the parameter 'userName' cannot be null");
        User user = request(userName);
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

    private User request(String userName) throws ExceptionClient {
        final URI queryByUsernameUri = queryByUsernameUriTemplate.expand(Map.of("username", userName)).toUri();
        log.debug("(UserServiceClient#request) request the user details from platform: {}", queryByUsernameUri);
        try {
            ResponseEntity<User> responseEntity = restTemplate.exchange(
                    queryByUsernameUri,
                    GET,
                    createRequestEntity(),
                    User.class);

            return responseEntity.getBody();
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
