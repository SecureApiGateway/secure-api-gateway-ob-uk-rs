/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.configuration.CloudClientConfiguration;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.model.User;

/**
 * Unit test for {@link UserClientService}
 */
@ActiveProfiles("test")
@RestClientTest(UserClientService.class)
@AutoConfigureWebClient(registerRestTemplate = true)
@Import(CloudClientConfiguration.class)
public class UserClientServiceTest {

    @Autowired
    private UserClientService userClientService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockRestServiceServer mockServer;

    private final static String USER_NAME = "psu4test";
    private final static String ACCOUNT_ACTIVE_STATUS = "active";

    @Test
    public void shouldGetUserByUsername() throws Exception {
        // Given
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .userName(USER_NAME)
                .accountStatus(ACCOUNT_ACTIVE_STATUS)
                .build();
        // When
        mockServer.expect(once(), requestTo("http://ig:80/repo/users?_queryFilter=userName+eq+%22psu4test%22"))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writeValueAsString(user)));

        User userResponse = userClientService.getUserByName(user.getUserName());
        // Then
        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getAccountStatus()).isEqualTo(ACCOUNT_ACTIVE_STATUS);
        assertThat(userResponse.getUserName()).isEqualTo(user.getUserName());
        assertThat(userResponse.getId()).isEqualTo(user.getId());
    }

    @Test
    public void ShouldRaiseNotFound_UserAccountNoActive() throws Exception {
        // Given
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .userName(USER_NAME)
                .accountStatus("inactive")
                .build();
        // when
        mockServer.expect(once(), requestTo("http://ig:80/repo/users?_queryFilter=userName+eq+%22psu4test%22"))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writeValueAsString(user)));


        ExceptionClient exception = catchThrowableOfType(() ->
                        userClientService.getUserByName(USER_NAME)
                , ExceptionClient.class
        );

        // Then
        assertThat(exception.getErrorClient().getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        assertThat(exception.getErrorClient().getErrorType().getErrorCode()).isEqualTo(ErrorType.NOT_FOUND.getErrorCode());
        assertThat(exception.getErrorClient().getErrorType().getInternalCode()).isEqualTo(ErrorType.NOT_FOUND.getInternalCode());
    }

    @Test
    public void ShouldRaiseNotFoundUserDataFromPlatform() {
        // When
        mockServer.expect(once(), requestTo("http://ig:80/repo/users?_queryFilter=userName+eq+%22psu4test%22"))
                  .andRespond(withStatus(HttpStatus.NOT_FOUND));

        ExceptionClient exception = catchThrowableOfType(() ->
                        userClientService.getUserByName(USER_NAME)
                , ExceptionClient.class
        );

        // Then
        assertThat(exception.getErrorClient().getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        assertThat(exception.getErrorClient().getErrorType().getErrorCode()).isEqualTo(ErrorType.NOT_FOUND.getErrorCode());
        assertThat(exception.getErrorClient().getErrorType().getInternalCode()).isEqualTo(ErrorType.NOT_FOUND.getInternalCode());
    }

    @Test
    public void ShouldRaiseParameterErrorWhenUserNull() {
        // When
        ExceptionClient exception = catchThrowableOfType(() ->
                        userClientService.getUserByName(null)
                , ExceptionClient.class
        );

        // Then
        assertThat(exception.getErrorClient().getErrorType()).isEqualTo(ErrorType.PARAMETER_ERROR);
        assertThat(exception.getErrorClient().getErrorType().getErrorCode()).isEqualTo(ErrorType.PARAMETER_ERROR.getErrorCode());
        assertThat(exception.getErrorClient().getErrorType().getInternalCode()).isEqualTo(ErrorType.PARAMETER_ERROR.getInternalCode());
    }

    @Test
    public void shouldGetUserById() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        User user = User.builder()
                .id(userId)
                .userName(USER_NAME)
                .accountStatus(ACCOUNT_ACTIVE_STATUS)
                .build();
        // When
        mockServer.expect(once(), requestTo("http://ig:80/repo/users/" + userId))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writeValueAsString(user)));

        User userResponse = userClientService.getUserById(userId);
        // Then
        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getAccountStatus()).isEqualTo(ACCOUNT_ACTIVE_STATUS);
        assertThat(userResponse.getUserName()).isEqualTo(user.getUserName());
        assertThat(userResponse.getId()).isEqualTo(user.getId());
    }

}
