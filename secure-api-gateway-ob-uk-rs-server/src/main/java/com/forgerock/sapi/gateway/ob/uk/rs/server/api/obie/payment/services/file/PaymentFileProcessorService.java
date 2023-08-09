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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.file;

import java.util.Set;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.PaymentFile;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.PaymentFileType;

/**
 * Service which is capable of processing uploaded files for OBIE File Payments and extracting the transactions and
 * metadata.
 */
public interface PaymentFileProcessorService {

    /**
     * Processes uploaded fileContents and returns a PaymentFile object containing the transactions described in the
     * file and associated metadata.
     *
     * @param fileType     String the OBIE schema fileType format that the fileContent is expected to be in
     * @param fileContents String the uploaded file content
     * @return PaymentFile object representing the parsed file
     * @throws OBErrorException if an error parsing the file occurs
     */
    PaymentFile processFile(String fileType, String fileContents) throws OBErrorException;

    /**
     * @return Set<String> all OBIE schema fileTypes supported by this service
     */
    Set<String> getSupportedFileTypes();

    /**
     * @param fileType String OBIE schema fileType value
     * @return PaymentFileType object representing the fileType
     * @throws OBErrorException thrown if the fileType is not supported
     */
    PaymentFileType findPaymentFileType(String fileType) throws OBErrorException;

}
