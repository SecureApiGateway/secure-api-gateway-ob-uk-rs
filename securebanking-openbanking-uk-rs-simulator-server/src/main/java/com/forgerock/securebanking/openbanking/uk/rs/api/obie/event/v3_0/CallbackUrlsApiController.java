/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.event.v3_0;


import com.forgerock.securebanking.openbanking.uk.common.api.meta.OBVersion;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorResponseException;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorResponseCategory;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.event.FRCallbackUrl;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.events.CallbackUrlsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.event.OBCallbackUrl1;
import uk.org.openbanking.datamodel.event.OBCallbackUrlsResponse1;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.*;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.event.FRCallbackUrlConverter.toFRCallbackUrlData;
import static com.forgerock.securebanking.openbanking.uk.rs.common.util.CallbackUrlsResponseUtil.packageResponse;
import static com.forgerock.securebanking.openbanking.uk.rs.common.util.VersionPathExtractor.getVersionFromPath;
import static com.forgerock.securebanking.openbanking.uk.rs.validator.ResourceVersionValidator.isAccessToResourceAllowed;
import static org.springframework.http.HttpStatus.*;

@Controller("CallbackUrlsApiV3.0")
@Slf4j
public class CallbackUrlsApiController implements CallbackUrlsApi {

    private final CallbackUrlsRepository callbackUrlsRepository;

    public CallbackUrlsApiController(CallbackUrlsRepository callbackUrlsRepository) {
        this.callbackUrlsRepository = callbackUrlsRepository;
    }

    @Override
    public ResponseEntity createCallbackUrls(
            @Valid OBCallbackUrl1 obCallbackUrl1,
            String xFapiFinancialId,
            String authorization,
            String xJwsSignature,
            String xFapiInteractionId,
            String tppId,
            HttpServletRequest request,
            Principal principal
    ) throws OBErrorResponseException {
        log.debug("Create new callback URL: {} for TPP: {}", obCallbackUrl1, tppId);
        String newUrl = obCallbackUrl1.getData().getUrl();

        // Check if callback URL already exists for TPP
        Collection<FRCallbackUrl> callbackUrls = callbackUrlsRepository.findByTppId(tppId);
        boolean callbackExists = callbackUrls.stream()
                .anyMatch(existingUrl -> newUrl.equals(existingUrl.getCallbackUrl().getUrl()));
        if (callbackExists) {
            log.debug("This callback URL: '{}' already exists for this TPP id: '{}'", newUrl, tppId);
            throw new OBErrorResponseException(
                    CONFLICT,
                    OBRIErrorResponseCategory.REQUEST_INVALID,
                    OBRIErrorType.CALLBACK_URL_ALREADY_EXISTS.toOBError1(newUrl)
            );
        }

        FRCallbackUrl frCallbackUrl = FRCallbackUrl.builder()
                .id(UUID.randomUUID().toString())
                .tppId(tppId)
                .callbackUrl(toFRCallbackUrlData(obCallbackUrl1))
                .build();
        callbackUrlsRepository.save(frCallbackUrl);

        return ResponseEntity
                .status(CREATED)
                .body(packageResponse(frCallbackUrl, getVersionFromPath(request), this.getClass()));
    }

    @Override
    public ResponseEntity<OBCallbackUrlsResponse1> readCallBackUrls(
            String xFapiFinancialId,
            String authorization,
            String xFapiInteractionId,
            String tppId,
            HttpServletRequest request,
            Principal principal
    ) {
        Collection<FRCallbackUrl> callbackUrls = callbackUrlsRepository.findByTppId(tppId);
        if (callbackUrls.isEmpty()) {
            log.warn("No CallbackURL found for tpp id '{}'", tppId);
            return ResponseEntity.ok(packageResponse(Collections.emptyList(), getVersionFromPath(request), this.getClass()));
        }
        // A TPP must only create a callback-url on one version
        return ResponseEntity.ok(packageResponse(new ArrayList<>(callbackUrls), getVersionFromPath(request), this.getClass()));
    }

    @Override
    public ResponseEntity updateCallbackUrl(
            String callbackUrlId,
            @Valid OBCallbackUrl1 obCallbackUrl1,
            String xFapiFinancialId,
            String authorization,
            String xJwsSignature,
            String xFapiInteractionId,
            String tppId,
            HttpServletRequest request,
            Principal principal
    ) throws OBErrorResponseException {
        Optional<FRCallbackUrl> byId = callbackUrlsRepository.findById(callbackUrlId);

        if (byId.isPresent()) {
            FRCallbackUrl frCallbackUrl = byId.get();
            OBVersion apiVersion = getVersionFromPath(request);
            OBVersion resourceVersion = OBVersion.fromString(frCallbackUrl.getCallbackUrl().getVersion());
            if (isAccessToResourceAllowed(apiVersion, resourceVersion)) {
                frCallbackUrl.setCallbackUrl(toFRCallbackUrlData(obCallbackUrl1));
                callbackUrlsRepository.save(frCallbackUrl);
                return ResponseEntity.ok(packageResponse(frCallbackUrl, apiVersion, this.getClass()));
            } else {
                return ResponseEntity
                        .status(CONFLICT)
                        .body("Callback URL: '" + callbackUrlId + "' can't be updated via an older API version.");
            }
        } else {
            // Spec isn't clear on if we should
            // 1. Reject a PUT for a resource id that does not exist
            // 2. Create a new resource for a PUT for resource id that does not exist
            // Option 2 is more restful but the examples in spec only use PUT for amending urls so currently I am implementing option 1.
            throw new OBErrorResponseException(
                    BAD_REQUEST,
                    OBRIErrorResponseCategory.REQUEST_INVALID,
                    OBRIErrorType.CALLBACK_URL_NOT_FOUND.toOBError1(callbackUrlId)
            );
        }
    }

    @Override
    public ResponseEntity deleteCallbackUrl(
            String callbackUrlId,
            String xFapiFinancialId,
            String authorization,
            String xFapiInteractionId,
            String tppId,
            HttpServletRequest request,
            Principal principal
    ) throws OBErrorResponseException {
        Optional<FRCallbackUrl> byId = callbackUrlsRepository.findById(callbackUrlId);
        if (byId.isPresent()) {
            OBVersion apiVersion = getVersionFromPath(request);
            OBVersion resourceVersion = OBVersion.fromString(byId.get().getCallbackUrl().getVersion());
            if (isAccessToResourceAllowed(apiVersion, resourceVersion)) {
                log.debug("Deleting callback url: {}", byId.get());
                callbackUrlsRepository.deleteById(callbackUrlId);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity
                        .status(CONFLICT)
                        .body("Callback URL: '" + callbackUrlId + "' can't be deleted via an older API version.");
            }
        } else {
            throw new OBErrorResponseException(
                    BAD_REQUEST,
                    OBRIErrorResponseCategory.REQUEST_INVALID,
                    OBRIErrorType.CALLBACK_URL_NOT_FOUND.toOBError1(callbackUrlId)
            );
        }
    }
}
