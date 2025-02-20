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
package com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Generic client cloud error types
 */
public enum ErrorType {

    // OIDC errors
    INTERACTION_REQUIRED("interaction_required", HttpStatus.BAD_REQUEST, ErrorType.OIDC_PREFIX_CODE + "0001",
            "The Authorization Server requires End-User interaction of some form to proceed."),

    LOGIN_REQUIRED("login_required", HttpStatus.UNAUTHORIZED, ErrorType.OIDC_PREFIX_CODE + "0002",
            "The Authorization Server requires End-User authentication. "),

    ACCOUNT_SELECTION_REQUIRED("account_selection_required", HttpStatus.PRECONDITION_REQUIRED, ErrorType.OIDC_PREFIX_CODE + "0003",
            "The End-User is REQUIRED to select a session at the Authorization Server."),

    CONSENT_REQUIRED("consent_required", HttpStatus.PRECONDITION_REQUIRED, ErrorType.OIDC_PREFIX_CODE + "0004",
            "The Authorization Server requires End-User consent."),

    INVALID_REQUEST_URI("invalid_request_uri", HttpStatus.BAD_REQUEST, ErrorType.OIDC_PREFIX_CODE + "0005",
            "The request_uri in the Authorization Request returns an error or contains invalid data."),

    INVALID_REQUEST_OBJECT("invalid_request_object", HttpStatus.BAD_REQUEST, ErrorType.OIDC_PREFIX_CODE + "0006",
            "The request parameter contains an invalid Request Object."),

    REQUEST_NOT_SUPPORTED("request_not_supported", HttpStatus.BAD_REQUEST, ErrorType.OIDC_PREFIX_CODE + "0007",
            "The OP does not support use of the request parameter defined in Section 6."),

    REQUEST_URI_NOT_SUPPORTED("request_uri_not_supported", HttpStatus.BAD_REQUEST, ErrorType.OIDC_PREFIX_CODE + "0008",
            "The OP does not support use of the request_uri parameter defined in Section 6."),

    REGISTRATION_NOT_SUPPORTED("request_not_supported", HttpStatus.BAD_REQUEST, ErrorType.OIDC_PREFIX_CODE + "0009",
            "The OP does not support use of the registration parameter defined in Section 7.2.1."),

    // OAUTH 2 errors
    INVALID_REQUEST("invalid_request", HttpStatus.BAD_REQUEST, ErrorType.OAUHT2_PREFIX_CODE + "0001",
            "The request is missing a required parameter, includes an invalid parameter value, " +
                    "includes a parameter more than once, or is otherwise malformed."),

    UNAUTHORIZED_CLIENT("unauthorized_client", HttpStatus.UNAUTHORIZED, ErrorType.OAUHT2_PREFIX_CODE + "0002",
            "The client is not authorized to request an authorization code using this method."),

    ACCESS_DENIED("access_denied", HttpStatus.UNAUTHORIZED, ErrorType.OAUHT2_PREFIX_CODE + "0003",
            "The resource owner or authorization server denied the request."),

    UNSUPPORTED_RESPONSE_TYPE("unsupported_response_type", HttpStatus.INTERNAL_SERVER_ERROR, ErrorType.OAUHT2_PREFIX_CODE + "0004",
            "The authorization server does not support obtaining an authorization code using this method."),

    INVALID_SCOPE("invalid_scope", HttpStatus.BAD_REQUEST, ErrorType.OAUHT2_PREFIX_CODE + "0005",
            "The requested scope is invalid, unknown, or malformed."),
    // 500 error
    SERVER_ERROR("server_error", HttpStatus.INTERNAL_SERVER_ERROR, ErrorType.OAUHT2_PREFIX_CODE + "0006",
            "The authorization server encountered an unexpected condition that prevented it " +
                    "from fulfilling the request."),
    // 503 error
    TEMPORARILY_UNAVAILABLE("temporarily_unavailable", HttpStatus.SERVICE_UNAVAILABLE, ErrorType.OAUHT2_PREFIX_CODE + "0007",
            "The authorization server is currently unable to handle the request due to a temporary " +
                    "overloading or maintenance of the server."),
    // Client errors
    NOT_FOUND("NOT_FOUND", HttpStatus.NOT_FOUND, ErrorType.CLIENT_PREFIX_CODE + "0001",
            "Object requested not found"),

    INTERNAL_SERVER_ERROR("internal_server_error", HttpStatus.INTERNAL_SERVER_ERROR, ErrorType.CLIENT_PREFIX_CODE + "0002",
            "The service encountered an unexpected condition that prevent it from fulfilling the request."),

    UNKNOWN_INTENT_TYPE("unknown_intent_type", HttpStatus.UNPROCESSABLE_ENTITY, ErrorType.CLIENT_PREFIX_CODE + "0003",
            "It has not been possible identify the intent type. Intent id prefix not valid."),

    PARAMETER_ERROR("parameter_error", HttpStatus.BAD_REQUEST, ErrorType.CLIENT_PREFIX_CODE + "0004",
            "The request contains invalid parameter."),

    REQUEST_BINDING_FAILED("request_binding_failed", HttpStatus.INTERNAL_SERVER_ERROR, ErrorType.CLIENT_PREFIX_CODE + "0005",
            "Request binding failed."),
    // Client JWT util errors
    JWT_INVALID("jwt_invalid", HttpStatus.INTERNAL_SERVER_ERROR, ErrorType.CLIENT_PREFIX_CODE + "0006",
            "The request parameter JWT is invalid. Reason '%s'");

    public static final String OIDC_PREFIX_CODE = "OIDC-";
    public static final String OAUHT2_PREFIX_CODE = "OAUTH-";
    public static final String CLIENT_PREFIX_CODE = "CLI-";
    private static final String OAUTH2_ERRORS_URI = "https://tools.ietf.org/html/rfc6749#section-4.1.2.1";
    private static final String OIDC_ERRORS_URI = "https://openid.net/specs/openid-connect-core-1_0.html#AuthError";
    private static final String CLIENT_ERRORS_URI = "https://www.forgerock.com";

    private final String errorCode;
    private final HttpStatus httpStatus;
    private final String internalCode;
    private final String description;

    ErrorType(String errorCode, HttpStatus httpStatus, String internalCode, String description) {
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.internalCode = internalCode;
        this.description = description;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getInternalCode() {
        return internalCode;
    }

    public String getDescription() {
        return description;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getErrorUri(String internalCode) {
        if (internalCode.startsWith(OIDC_PREFIX_CODE)) {
            return OIDC_ERRORS_URI;
        } else if (internalCode.startsWith(OAUHT2_PREFIX_CODE)) {
            return OAUTH2_ERRORS_URI;
        } else {
            return CLIENT_ERRORS_URI;
        }
    }
}
