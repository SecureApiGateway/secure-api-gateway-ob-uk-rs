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
package com.forgerock.sapi.gateway.rs.resource.store.api.admin.exceptions;

import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ErrorClient;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ExceptionClient;
import lombok.Value;

@Value
public class DataApiException extends RuntimeException {
    ErrorType errorType;
    String reason;
    String apiClientId;
    String consentId;
    String userName;
    String userId;

    public DataApiException(ExceptionClient exceptionClient) {
        super(exceptionClient.getErrorClient().getReason());
        this.reason = exceptionClient.getErrorClient().getReason();
        this.errorType = exceptionClient.getErrorClient().getErrorType();
        this.apiClientId = exceptionClient.getErrorClient().getApiClientId();
        this.consentId = exceptionClient.getErrorClient().getIntentId();
        this.userId = exceptionClient.getErrorClient().getUserId();
        this.userName = exceptionClient.getErrorClient().getUserName();
    }

    public DataApiException(ExceptionClient exceptionClient, String reason) {
        super(reason);
        this.reason = reason;
        this.errorType = exceptionClient.getErrorClient().getErrorType();
        this.apiClientId = exceptionClient.getErrorClient().getApiClientId();
        this.consentId = exceptionClient.getErrorClient().getIntentId();
        this.userId = exceptionClient.getErrorClient().getUserId();
        this.userName = exceptionClient.getErrorClient().getUserName();
    }

    public DataApiException(ErrorClient errorClient) {
        super(errorClient.getReason());
        this.reason = errorClient.getReason();
        this.errorType = errorClient.getErrorType();
        this.apiClientId = errorClient.getApiClientId();
        this.consentId = errorClient.getIntentId();
        this.userId = errorClient.getUserId();
        this.userName = errorClient.getUserName();
    }
    public DataApiException(ErrorType errorType, String apiClientId, String consentId, String userName, String userId) {
        super(errorType.getDescription());
        this.errorType = errorType;
        this.reason = errorType.getDescription();
        this.apiClientId = apiClientId;
        this.consentId = consentId;
        this.userName = userName;
        this.userId = userId;
    }

    public DataApiException(ErrorType errorType, String reason, String apiClientId, String consentId, String userName, String userId) {
        super(reason);
        this.errorType = errorType;
        this.reason = reason;
        this.apiClientId = apiClientId;
        this.consentId = consentId;
        this.userName = userName;
        this.userId = userId;
    }

    public DataApiException(Throwable cause, ErrorType errorType, String apiClientId, String consentId, String userName, String userId) {
        super(errorType.getDescription(), cause);
        this.errorType = errorType;
        this.reason = errorType.getDescription();
        this.apiClientId = apiClientId;
        this.consentId = consentId;
        this.userName = userName;
        this.userId = userId;
    }
}
