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

import static com.forgerock.sapi.gateway.ob.uk.common.error.ErrorCode.FR_OBRI_IDEMPOTENCY_KEY_REQUEST_BODY_CHANGED;
import static com.forgerock.sapi.gateway.ob.uk.common.error.ErrorCode.OBRI_CONSENT_NOT_FOUND;
import static com.forgerock.sapi.gateway.ob.uk.common.error.ErrorCode.OBRI_PERMISSION_INVALID;
import static com.forgerock.sapi.gateway.ob.uk.common.error.ErrorCode.OBRI_SERVER_INTERNAL_ERROR;
import static uk.org.openbanking.datamodel.error.OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_CONSENT_STATUS;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException;

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

        final String fapiInteractionId = getFapiInteractionId(request);
        log.info("({}) Request failed due to OBErrorResponseException - status: {}, category: {}, errors: {}",
                fapiInteractionId, ex.getStatus(), ex.getCategory(), ex.getErrors());

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

        final String fapiInteractionId = getFapiInteractionId(request);
        log.info("({}) Request failed due to exception - message: {}", fapiInteractionId, ex.getMessage());

        HttpStatus httpStatus = ex.getObriErrorType().getHttpStatus();
        return ResponseEntity.status(httpStatus).body(
                new OBErrorResponse1()
                        .code(httpStatus.name())
                        .id(getFapiInteractionId(request))
                        .message(httpStatus.getReasonPhrase())
                        .errors(Collections.singletonList(ex.getOBError())));
    }

    @ExceptionHandler(value = {HttpMessageConversionException.class})
    protected ResponseEntity<Object> handleHttpMessageConversionException(HttpMessageConversionException ex,
                                                                          WebRequest request) {

        log.info("({}) An invalid resource format ", getFapiInteractionId(request),ex);
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

    @ExceptionHandler(ConsentStoreClientException.class)
    protected ResponseEntity<OBErrorResponse1> handleConsentStoreClientException(ConsentStoreClientException ex, WebRequest request) {
        final String fapiInteractionId = getFapiInteractionId(request);

        // Omit the stacktrace as most of the exceptions are due to bad client requests
        log.info("({}) request failed due to Consent Store Exception: {}", fapiInteractionId, ex.getMessage());

        final HttpStatus httpStatus;
        final String errorCode;
        final String message;
        switch (ex.getErrorType()) {
            case INVALID_PERMISSIONS:
                httpStatus = HttpStatus.FORBIDDEN;
                errorCode = OBRI_PERMISSION_INVALID.toString();
                message = "You are not allowed to access this consent";
                break;
            case NOT_FOUND:
                httpStatus = HttpStatus.NOT_FOUND;
                errorCode = OBRI_CONSENT_NOT_FOUND.toString();
                message = "Consent not found";
                break;
            case INVALID_STATE_TRANSITION:
                httpStatus = HttpStatus.BAD_REQUEST;
                errorCode = UK_OBIE_RESOURCE_INVALID_CONSENT_STATUS.toString();
                message = ex.getMessage();
                break;
            case IDEMPOTENCY_ERROR:
                httpStatus = HttpStatus.BAD_REQUEST;
                errorCode = FR_OBRI_IDEMPOTENCY_KEY_REQUEST_BODY_CHANGED.toString();
                message = ex.getMessage();
                break;
            default:
                // Handle as an unexpected exception which yields internal server error, log stacktrace for debugging
                log.warn("({}) Unexpected ConsentStoreClientException", fapiInteractionId, ex);
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
                errorCode = OBRI_SERVER_INTERNAL_ERROR.toString();
                message = ex.getMessage();
        }

        final String errorResponseId = fapiInteractionId != null ? fapiInteractionId : UUID.randomUUID().toString();

        return ResponseEntity.status(httpStatus).body(new OBErrorResponse1().code(errorCode)
                .id(errorResponseId)
                .message(httpStatus.getReasonPhrase())
                .errors(List.of(new OBError1().errorCode(errorCode)
                                              .message(message))));
    }

    /**
     * Handler for unexpected RuntimeExceptions (such as NullPointerException).
     *
     * Spring will only call this handler if a more specific handler cannot be found, the ConsentStoreException is a
     * RuntimeException but this has its own handler method.
     *
     * As these are unexpected, then we log at error level and include the stacktrace to aid debugging.
     */
    @ExceptionHandler(value = {RuntimeException.class})
    protected ResponseEntity<Object> handleUnexpectedRuntimeException(RuntimeException ex,
                                                                      WebRequest request) {
        log.error("({}) Unexpected exception thrown processing request - returning 500 error", getFapiInteractionId(request), ex);
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(httpStatus).body(
                new OBErrorResponse1()
                        .code(httpStatus.name())
                        .id(getFapiInteractionId(request))
                        .message(httpStatus.getReasonPhrase()));
    }

    private static String getFapiInteractionId(WebRequest request) {
        return request.getHeader("x-fapi-interaction-id");
    }
}
