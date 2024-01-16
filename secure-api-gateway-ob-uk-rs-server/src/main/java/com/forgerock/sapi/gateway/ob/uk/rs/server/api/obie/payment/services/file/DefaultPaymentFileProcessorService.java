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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.file;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.PaymentFile;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.PaymentFileType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.processor.PaymentFileProcessor;

/**
 * Default implementation of {@link PaymentFileProcessorService}, capable of processing payment files uploaded for
 * supported FileTypes.
 *
 * This service contains a registry which maps OBIE schema OBFile2/FileType String values to {@link PaymentFileProcessor}
 * objects. The PaymentFileProcessors are then delegated to for the real processing work.
 *
 * An exception is raised when attempting to process an unsupported FileType.
 */
public class DefaultPaymentFileProcessorService implements PaymentFileProcessorService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, PaymentFileType> fileTypeRegistry;
    private final Map<PaymentFileType, PaymentFileProcessor> fileTypeProcessorRegistry;

    public DefaultPaymentFileProcessorService(List<PaymentFileProcessor> paymentFileProcessors) {
        if (paymentFileProcessors == null || paymentFileProcessors.isEmpty()) {
            throw new IllegalArgumentException("1 or more paymentFileProcessors must be supplied");
        }
        Map<String, PaymentFileType> fileTypeRegistry = new HashMap<>();
        Map<PaymentFileType, PaymentFileProcessor> fileTypeProcessorRegistry = new HashMap<>();
        for (final PaymentFileProcessor paymentFileProcessor : paymentFileProcessors) {
            final PaymentFileType supportedFileType = paymentFileProcessor.getSupportedFileType();
            final PaymentFileType oldFileTypeValue = fileTypeRegistry.put(supportedFileType.getFileType(), supportedFileType);
            if (oldFileTypeValue != null) {
                throw new IllegalStateException("Duplicate paymentFileProcessor for fileType: " + supportedFileType.getFileType());
            }
            fileTypeProcessorRegistry.put(supportedFileType, paymentFileProcessor);
        }

        this.fileTypeRegistry = Collections.unmodifiableMap(fileTypeRegistry);
        this.fileTypeProcessorRegistry = Collections.unmodifiableMap(fileTypeProcessorRegistry);
        logger.info("Supported File Payment Types: {}", fileTypeProcessorRegistry);
    }

    @Override
    public Set<String> getSupportedFileTypes() {
        return fileTypeRegistry.keySet();
    }

    @Override
    public PaymentFileType findPaymentFileType(String fileType) throws OBErrorException {
        final PaymentFileType paymentFileType = fileTypeRegistry.get(fileType);
        if (paymentFileType == null) {
            throw new OBErrorException(OBRIErrorType.REQUEST_FILE_TYPE_NOT_SUPPORTED, fileType);
        }
        return paymentFileType;
    }

    @Override
    public PaymentFile processFile(String fileType, String fileContents) throws OBErrorException {
        final PaymentFileProcessor paymentFileProcessor = fileTypeProcessorRegistry.get(findPaymentFileType(fileType));
        if (paymentFileProcessor == null) {
            throw new OBErrorException(OBRIErrorType.REQUEST_FILE_TYPE_NOT_SUPPORTED, fileType);
        }
        try {
            return paymentFileProcessor.processFile(fileContents);
        } catch (OBErrorException ex) {
            throw ex;
        } catch (Throwable t) {
            // Guard against unexpected exceptions being raised by the processor impl
            logger.error("Unexpected exception raised processing payment file of type: {}, processorClass: {}",
                    fileType, paymentFileProcessor, t);
            throw new OBErrorException(OBRIErrorType.REQUEST_FILE_INVALID, "Failed to parse");
        }
    }
}
