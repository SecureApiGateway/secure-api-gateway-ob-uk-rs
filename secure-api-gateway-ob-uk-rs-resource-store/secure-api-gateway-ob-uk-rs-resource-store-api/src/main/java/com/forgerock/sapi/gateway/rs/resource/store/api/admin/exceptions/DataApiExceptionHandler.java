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

import com.forgerock.sapi.gateway.rs.resource.store.api.ResourceStoreApiModuleConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.error.OBErrorResponse1;

import java.util.Collections;

@ControllerAdvice(basePackageClasses = {ResourceStoreApiModuleConfiguration.class})
@Slf4j
public class DataApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {DataApiException.class})
    protected ResponseEntity<Object> handleDataApiException(DataApiException ex, WebRequest request) {
        HttpStatus httpStatus = ex.getErrorType().getHttpStatus();
        log.debug("Error in admin data user API, reason {}", ex.getMessage());
        return ResponseEntity.status(httpStatus).body(
                new OBErrorResponse1()
                        .code(httpStatus.name())
                        .id(request.getHeader("x-fapi-interaction-id"))
                        .message(httpStatus.getReasonPhrase())
                        .errors(
                                Collections.singletonList(new OBError1().message(ex.getMessage()))
                        )
        );
    }
}
