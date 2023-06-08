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
package com.forgerock.sapi.gateway.ob.uk.rs.server.exceptions;

import static com.forgerock.sapi.gateway.ob.uk.common.error.ErrorCode.OBRI_CONSENT_NOT_FOUND;
import static com.forgerock.sapi.gateway.ob.uk.common.error.ErrorCode.OBRI_PERMISSION_INVALID;
import static com.forgerock.sapi.gateway.ob.uk.common.error.ErrorCode.OBRI_SERVER_INTERNAL_ERROR;
import static uk.org.openbanking.datamodel.error.OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_CONSENT_STATUS;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.rcs.conent.store.client.ConsentStoreClientException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.error.OBErrorResponse1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        List<OBError1> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(OBRIErrorType.REQUEST_FIELD_INVALID
                    .toOBError1(error.getDefaultMessage())
                    .path(error.getField())
            );
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(OBRIErrorType.REQUEST_OBJECT_INVALID
                    .toOBError1(error.getDefaultMessage())
                    .path(error.getObjectName())
            );
        }

        return handleOBErrorResponse(
                new OBErrorResponseException(
                        HttpStatus.BAD_REQUEST,
                        OBRIErrorResponseCategory.ARGUMENT_INVALID,
                        errors),
                request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                          HttpHeaders headers,
                                                                          HttpStatus status,
                                                                          WebRequest request) {
        return handleOBErrorResponse(
                new OBErrorResponseException(
                        HttpStatus.BAD_REQUEST,
                        OBRIErrorResponseCategory.REQUEST_INVALID,
                        OBRIErrorType.REQUEST_PARAMETER_MISSING
                                .toOBError1(ex.getParameterName()).path(ex.getParameterName())),
                request);
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex,
                                                                          HttpHeaders headers,
                                                                          HttpStatus status,
                                                                          WebRequest request) {
        if (ex.getMessage().startsWith("Missing request header")) {
            return handleOBErrorResponse(
                    new OBErrorResponseException(
                            HttpStatus.BAD_REQUEST,
                            OBRIErrorResponseCategory.REQUEST_INVALID,
                            OBRIErrorType.REQUEST_MISSING_HEADER.toOBError1(ex.getMessage())
                    ),
                    request);
        } else if (ex.getMessage().startsWith("Missing cookie")) {
            return handleOBErrorResponse(
                    new OBErrorResponseException(
                            HttpStatus.BAD_REQUEST,
                            OBRIErrorResponseCategory.REQUEST_INVALID,
                            OBRIErrorType.REQUEST_MISSING_COOKIE.toOBError1(ex.getMessage())
                    ),
                    request);
        } else if (ex.getMessage().startsWith("Missing argument")) {
            return handleOBErrorResponse(
                    new OBErrorResponseException(
                            HttpStatus.BAD_REQUEST,
                            OBRIErrorResponseCategory.REQUEST_INVALID,
                            OBRIErrorType.REQUEST_MISSING_ARGUMENT.toOBError1(ex.getMessage())
                    ),
                    request);
        }
        return handleOBErrorResponse(
                new OBErrorResponseException(
                        HttpStatus.BAD_REQUEST,
                        OBRIErrorResponseCategory.REQUEST_INVALID,
                        OBRIErrorType.REQUEST_BINDING_FAILED.toOBError1(ex.getLocalizedMessage())
                ),
                request);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                                   WebRequest request) {
        return handleOBErrorResponse(
                new OBErrorResponseException(
                        HttpStatus.BAD_REQUEST,
                        OBRIErrorResponseCategory.REQUEST_INVALID,
                        OBRIErrorType.REQUEST_ARGUMENT_TYPE_MISMATCH
                                .toOBError1(ex.getName(), ex.getRequiredType().getName())
                ),
                request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                         HttpHeaders headers,
                                                                         HttpStatus status,
                                                                         WebRequest request) {
        StringBuilder builder = new StringBuilder();
        ex.getSupportedHttpMethods().forEach(t -> builder.append("'").append(t).append("' "));

        return handleOBErrorResponse(
                new OBErrorResponseException(
                        HttpStatus.BAD_REQUEST,
                        OBRIErrorResponseCategory.REQUEST_INVALID,
                        OBRIErrorType.REQUEST_METHOD_NOT_SUPPORTED.toOBError1(ex.getMethod(), builder.toString())
                ),
                request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
                                                                     HttpHeaders headers,
                                                                     HttpStatus status,
                                                                     WebRequest request) {
        StringBuilder builder = new StringBuilder();
        ex.getSupportedMediaTypes().forEach(t -> builder.append("'").append(t).append("' "));

        return handleOBErrorResponse(
                new OBErrorResponseException(
                        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                        OBRIErrorResponseCategory.REQUEST_INVALID,
                        OBRIErrorType.REQUEST_MEDIA_TYPE_NOT_SUPPORTED
                                .toOBError1(ex.getContentType(), builder.toString())
                ),
                request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        log.debug("HttpMessageNotReadableException from request: {}", request, ex);
        return handleOBErrorResponse(
                new OBErrorResponseException(
                        status,
                        OBRIErrorResponseCategory.REQUEST_INVALID,
                        OBRIErrorType.REQUEST_MESSAGE_NOT_READABLE
                                .toOBError1((ex.getCause() != null) ? ex.getCause().getMessage() : ex.getMessage())
                ),
                request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex,
                                                                      HttpHeaders headers,
                                                                      HttpStatus status,
                                                                      WebRequest request) {
        StringBuilder builder = new StringBuilder();
        ex.getSupportedMediaTypes().forEach(t -> builder.append("'").append(t).append("' "));

        return handleOBErrorResponse(
                new OBErrorResponseException(
                        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                        OBRIErrorResponseCategory.REQUEST_INVALID,
                        OBRIErrorType.REQUEST_MEDIA_TYPE_NOT_ACCEPTABLE.toOBError1(builder.toString())
                ),
                request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex,
                                                               HttpHeaders headers,
                                                               HttpStatus status,
                                                               WebRequest request) {
        return handleOBErrorResponse(
                new OBErrorResponseException(
                        status,
                        OBRIErrorResponseCategory.REQUEST_INVALID,
                        OBRIErrorType.REQUEST_PATH_VARIABLE_MISSING.toOBError1(ex.getVariableName(), ex.getParameter())
                ),
                request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             Object body,
                                                             HttpHeaders headers,
                                                             HttpStatus status,
                                                             WebRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            handleOBErrorResponse(
                    new OBErrorResponseException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            OBRIErrorResponseCategory.SERVER_INTERNAL_ERROR, OBRIErrorType.SERVER_ERROR.toOBError1()
                    ),
                    request);
        }
        handleOBErrorResponse(
                new OBErrorResponseException(
                        status,
                        OBRIErrorResponseCategory.REQUEST_INVALID,
                        OBRIErrorType.REQUEST_UNDEFINED_ERROR_YET.toOBError1(ex.getMessage())
                ),
                request);
        return new ResponseEntity<>(body, headers, status);
    }

    @ExceptionHandler(value = {OBErrorResponseException.class})
    protected ResponseEntity<Object> handleOBErrorResponse(OBErrorResponseException ex,
                                                           WebRequest request) {

        final String fapiInteractionId = request.getHeader("x-fapi-interaction-id");
        log.info("({}) Request failed due to exception - message: {}", fapiInteractionId, ex.getMessage());

        return ResponseEntity.status(ex.getStatus()).body(
                new OBErrorResponse1()
                        .code(ex.getCategory().getId())
                        .id(ex.getId() != null ? ex.getId() : fapiInteractionId)
                        .message(ex.getCategory().getDescription())
                        .errors(ex.getErrors()));
    }


    @ExceptionHandler(value = {OBErrorException.class})
    protected ResponseEntity<Object> handleOBError(OBErrorException ex,
                                                   WebRequest request) {

        final String fapiInteractionId = request.getHeader("x-fapi-interaction-id");
        log.info("({}) Request failed due to exception - message: {}", fapiInteractionId, ex.getMessage());

        HttpStatus httpStatus = ex.getObriErrorType().getHttpStatus();
        return ResponseEntity.status(httpStatus).body(
                new OBErrorResponse1()
                        .code(httpStatus.name())
                        .id(request.getHeader("x-fapi-interaction-id"))
                        .message(httpStatus.getReasonPhrase())
                        .errors(Collections.singletonList(ex.getOBError())));
    }

    // Required here because these programming errors can get lost in Spring handlers making debug very difficult
    @ExceptionHandler(value = {IllegalArgumentException.class})
    protected ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex,
                                                           WebRequest request) {
        log.error("Internal server error from an IllegalArgumentException", ex);
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(httpStatus).body(
                new OBErrorResponse1()
                        .code(httpStatus.name())
                        .id(request.getHeader("x-fapi-interaction-id"))
                        .message(httpStatus.getReasonPhrase()));
    }

    @ExceptionHandler(value = {HttpMessageConversionException.class})
    protected ResponseEntity<Object> handleHttpMessageConversionException(HttpMessageConversionException ex,
                                                                          WebRequest request) {
        log.debug("An invalid resource format ", ex);
        return handleOBErrorResponse(
                new OBErrorResponseException(
                        HttpStatus.BAD_REQUEST,
                        OBRIErrorResponseCategory.REQUEST_INVALID,
                        OBRIErrorType.INVALID_RESOURCE
                                .toOBError1((ex.getCause() != null) ?
                                        ((ex.getCause().getCause() != null) ? ex.getCause().getCause().getMessage() :
                                                ex.getCause().getMessage())
                                        : ex.getMessage())
                ),
                request);
    }

    @ExceptionHandler(value = {InvalidConsentException.class})
    protected ResponseEntity<Object> handleInvalidConsentException(InvalidConsentException ex, WebRequest request) {
        HttpStatus httpStatus = ex.getObriErrorType().getHttpStatus();
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

    @ExceptionHandler(ConsentStoreClientException.class)
    protected ResponseEntity<OBErrorResponse1> handleConsentStoreClientException(ConsentStoreClientException ex, WebRequest request) {
        final String fapiInteractionId = request.getHeader("x-fapi-interaction-id");

        // Omit the stacktrace as most of the exceptions are due to bad client requests
        log.info("({}) request failed due to Consent Store Exception: {}", fapiInteractionId, ex.getMessage());

        final HttpStatus httpStatus;
        final String errorCode;
        switch (ex.getErrorType()) {
        case INVALID_PERMISSIONS:
            httpStatus = HttpStatus.FORBIDDEN;
            errorCode = OBRI_PERMISSION_INVALID.toString();
            break;
        case NOT_FOUND:
            httpStatus = HttpStatus.NOT_FOUND;
            errorCode = OBRI_CONSENT_NOT_FOUND.toString();
            break;
        case INVALID_STATE_TRANSITION:
            httpStatus = HttpStatus.BAD_REQUEST;
            errorCode = UK_OBIE_RESOURCE_INVALID_CONSENT_STATUS.toString();
            break;
        default:
            // Handle as an unexpected exception which yields internal server error, log stacktrace for debugging
            log.warn("({}) Unexpected ConsentStoreClientException", fapiInteractionId, ex);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            errorCode = OBRI_SERVER_INTERNAL_ERROR.toString();
        }

        final String errorResponseId = fapiInteractionId != null ? fapiInteractionId : UUID.randomUUID().toString();

        return ResponseEntity.status(httpStatus).body(new OBErrorResponse1().code(errorCode)
                .id(errorResponseId)
                .message(httpStatus.name())
                .errors(List.of(new OBError1().errorCode(ex.getErrorType().name())
                        .message(ex.getMessage()))));
    }
}
