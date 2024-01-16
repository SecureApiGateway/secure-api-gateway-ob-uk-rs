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
package com.forgerock.sapi.gateway.ob.uk.rs.server.common;

/**
 * All the constants defined by the OIDC standard.
 */
public class OIDCConstants {

    private OIDCConstants() {}

    public static class Endpoint {
        private Endpoint() {}
        public static final String WELL_KNOWN = ".well-known/openid-configuration";
    }

    public static class SubjectType {
        private SubjectType() {}
        public static final String PUBLIC = "public";
        public static final String PAIRWISE = "pairwise";
    }

    public enum TokenEndpointAuthMethods {
        CLIENT_SECRET_POST("client_secret_post"),
        CLIENT_SECRET_BASIC("client_secret_basic"),
        CLIENT_SECRET_JWT("client_secret_jwt"),
        TLS_CLIENT_AUTH("tls_client_auth"),
        PRIVATE_KEY_JWT("private_key_jwt");

        public final String type;

        TokenEndpointAuthMethods(String type) {
            this.type = type;
        }

        public static TokenEndpointAuthMethods fromType(String type) {
            for (TokenEndpointAuthMethods tokenEndpointAuthMethods : TokenEndpointAuthMethods.values()) {
                if (tokenEndpointAuthMethods.type.equals(type)) {
                    return tokenEndpointAuthMethods;
                }
            }
            throw new IllegalArgumentException("Type '" + type + "' doesn't match any of the token endpoint auth methods");
        }
    }

    public static class ResponseType {
        private ResponseType() {}
        public static final String CODE = "code";
        public static final String ID_TOKEN = "id_token";
        public static final String TOKEN = "token";
    }

    public enum GrantType {
        CLIENT_CREDENTIAL("client_credentials"),
        AUTHORIZATION_CODE("authorization_code"),
        PASSWORD("password"),
        REFRESH_TOKEN("refresh_token"),
        HEADLESS_AUTH("headless_auth"),
        ;
        public final String type;

        GrantType(String type) {
            this.type = type;
        }

        public static GrantType fromType(String type) {
            for (GrantType grantType : GrantType.values()) {
                if (grantType.type.equals(type)) {
                    return grantType;
                }
            }
            throw new IllegalArgumentException("Type '" + type + "' doesn't match any of the grant type");
        }
    }

    public static class OIDCClaim {
        private OIDCClaim() {}
        public static final String GRANT_TYPE = "grant_type";
        public static final String ID_TOKEN = "id_token";
        public static final String USER_INFO = "userinfo";
        public static final String CLAIMS = "claims";
        public static final String EXP = "exp";
        public static final String CONSENT_APPROVAL_REDIRECT_URI = "consentApprovalRedirectUri";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_ID = "client_id";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String REQUEST = "request";
        public static final String SCOPE = "scope";
        public static final String STATE = "state";
        public static final String NONCE = "nonce";
        public static final String CLIENT_ASSERTION_TYPE = "client_assertion_type";
        public static final String CLIENT_ASSERTION = "client_assertion";
        public static final String CODE = "code";
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String OB_ACR_SCA_VALUE = "urn:openbanking:psd2:sca";
        public static final String OB_ACR_CA_VALUE = "urn:openbanking:psd2:ca";
        public static final String MAX_AGE = "max_age";
    }

}
