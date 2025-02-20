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
package com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.utils.jwt;

import com.forgerock.sapi.gateway.uk.common.shared.claim.Claims;
import com.forgerock.sapi.gateway.uk.common.shared.claim.JwsClaimsUtils;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ErrorClient;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ExceptionClient;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetVerifier;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import static com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ErrorType.JWT_INVALID;

/**
 * Utility to handling the JWT's and JWS's and {@link ParseException}
 */
@Slf4j
public class JwtUtil {

    public static final SignedJWT getSignedJWT(String jwt) throws ExceptionClient {
        try {
            log.debug("(JwtRcsUtil#getSignedJWT) Parsing the jws [{}]", jwt);
            return (SignedJWT) JWTParser.parse(jwt);
        } catch (ParseException exception) {
            log.error("(JwtRcsUtil#getSignedJWT) Could not parse the JWS.", exception);
            throw new ExceptionClient(
                    ErrorClient.builder()
                            .errorType(JWT_INVALID)
                            .build(),
                    String.format(JWT_INVALID.getDescription(), exception.getMessage()),
                    exception
            );
        }
    }

    public static final Claims getClaims(SignedJWT signedJWT) throws ExceptionClient {
        try {
            log.debug("(JwtRcsUtil#getClaims) Parsing the jws [{}] to retrieve the claims", signedJWT.getParsedString());
            return JwsClaimsUtils.getClaims(signedJWT);
        } catch (ParseException exception) {
            log.error("(JwtRcsUtil#getClaims) Could not parse the JWT to retrieve the claims", exception);
            throw new ExceptionClient(
                    ErrorClient.builder()
                            .errorType(JWT_INVALID)
                            .build(),
                    String.format(JWT_INVALID.getDescription(), exception.getMessage()),
                    exception
            );
        }
    }

    public static final List<String> getAudiences(String jwt) throws ExceptionClient {
        try {
            log.debug("(JwtRcsUtil#getAudience) Getting the audience from:\n'{}'\n", jwt);
            List<String> audiences = getSignedJWT(jwt).getJWTClaimsSet().getAudience();
            if(audiences.isEmpty()){
                String message = String.format("Could not get the audience (aud) from the JWT %s", jwt);
                log.error("(JwtRcsUtil#getAudiences) {}", message);
                throw new ExceptionClient(
                        ErrorClient.builder()
                                .errorType(JWT_INVALID)
                                .build(),
                        String.format(JWT_INVALID.getDescription(), message)
                );
            }
            return audiences;
        }catch (ParseException exception){
            log.error("(JwtRcsUtil#getAudience) Could not parse the JWT to retrieve the audience", exception);
            throw new ExceptionClient(
                    ErrorClient.builder()
                            .errorType(JWT_INVALID)
                            .build(),
                    String.format(JWT_INVALID.getDescription(), exception.getMessage()),
                    exception
            );
        }
    }

    public static final String getIdTokenClaim(String jwt, String idTokenClaim) throws ExceptionClient {
        return getClaims(getSignedJWT(jwt)).getIdTokenClaims().get(idTokenClaim).getValue();
    }

    public static final String getIdTokenClaim(SignedJWT signedJWT, String idTokenClaim) throws ExceptionClient {
        return getClaims(signedJWT).getIdTokenClaims().get(idTokenClaim).getValue();
    }

    public static final String getIdTokenClaim(Claims claims, String idTokenClaim) {
        return claims.getIdTokenClaims().get(idTokenClaim).getValue();
    }

    public static final String getClaimValue(String jwt, String claim) throws ExceptionClient {
        try {
            log.debug("(JwtRcsUtil#getClaimValue) Parsing the jws [{}] to retrieve the claim '{}' value", jwt, claim);
            return getSignedJWT(jwt).getJWTClaimsSet().getStringClaim(claim);
        } catch (ParseException exception) {
            log.error("(JwtRcsUtil#getClaimValue) Could not parse the JWT to retrieve the claim '{}' value", claim, exception);
            throw new ExceptionClient(
                    ErrorClient.builder()
                            .errorType(JWT_INVALID)
                            .build(),
                    String.format(JWT_INVALID.getDescription(), exception.getMessage()),
                    exception
            );
        }
    }

    public static final Map<String, Object> getClaimValueMap(SignedJWT signedJWT, String claim) throws ExceptionClient {
        try {
            log.debug("(JwtRcsUtil#getClaimValue) Parsing the jws [{}] to retrieve the map from json claim '{}' value",
                    signedJWT.getParsedString(),
                    claim);
            return signedJWT.getJWTClaimsSet().getJSONObjectClaim(claim);
        } catch (ParseException exception) {
            log.error("(JwtRcsUtil#getClaimValue) Could not parse the JWT to retrieve the claim '{}' value", claim, exception);
            throw new ExceptionClient(
                    ErrorClient.builder()
                            .errorType(JWT_INVALID)
                            .build(),
                    String.format(JWT_INVALID.getDescription(), exception.getMessage()),
                    exception
            );
        }
    }

    public static final String getClaimValue(SignedJWT signedJWT, String claim) throws ExceptionClient {
        try {
            log.debug("(JwtRcsUtil#getClaimValue) Parsing the jws [{}] to retrieve the string claim '{}' value",
                    signedJWT.getParsedString(),
                    claim);
            return signedJWT.getJWTClaimsSet().getStringClaim(claim);
        } catch (ParseException exception) {
            log.error("(JwtRcsUtil#getClaimValue) Could not parse the JWT to retrieve the claim '{}' value", claim, exception);
            throw new ExceptionClient(
                    ErrorClient.builder()
                            .errorType(JWT_INVALID)
                            .build(),
                    String.format(JWT_INVALID.getDescription(), exception.getMessage()),
                    exception
            );
        }
    }

    public static final boolean validateJWT(String jwt, String jwkUri) throws ExceptionClient {
        log.debug("(JwtRcsUtil#validateJWT(string)) Validating the jwt [{}]", jwt);
        return validateJWT(getSignedJWT(jwt), jwkUri);
    }

    public static final boolean validateJWT(SignedJWT signedJWT, String jwkUri) throws ExceptionClient {
        log.debug("(JwtRcsUtil#validateJWT(signedJWT)) Validating the jwt [{}]", signedJWT.getParsedString());
        try {
            if (signedJWT.getHeader().getAlgorithm() != null && jwkUri != null) {
                JWSAlgorithm expectedJWSAlg = JWSAlgorithm.parse(signedJWT.getHeader().getAlgorithm().getName());
                JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL(jwkUri));
                JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(expectedJWSAlg, keySource);

                // Create a JWT processor
                ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
                jwtProcessor.setJWSKeySelector(keySelector);
                JWTClaimsSetVerifier<SecurityContext> claimsVerifier = new DefaultJWTClaimsVerifier<>();
                jwtProcessor.setJWTClaimsSetVerifier(claimsVerifier);

                // Process the JWT
                jwtProcessor.process(signedJWT, null);
            }
            return true;
        } catch (BadJOSEException | JOSEException | MalformedURLException exception) {
            String messageError = String.format("Error verifying the consent request JWT. Reason: %s",
                    exception.getMessage());
            log.error(messageError);
            throw new ExceptionClient(
                    ErrorClient.builder()
                            .errorType(JWT_INVALID)
                            .build(),
                    String.format(JWT_INVALID.getDescription(), exception.getMessage()),
                    exception
            );
        }
    }

}
