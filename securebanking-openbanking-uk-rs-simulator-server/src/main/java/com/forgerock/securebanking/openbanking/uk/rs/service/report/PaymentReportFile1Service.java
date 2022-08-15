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
package com.forgerock.securebanking.openbanking.uk.rs.service.report;

import com.forgerock.securebanking.openbanking.uk.error.OBErrorResponseException;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.payment.FRFilePaymentSubmission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Generate a file payment report from an authorised payment consent.
 */
@Service
@Slf4j
public class PaymentReportFile1Service {

//    private final OBIEPaymentInitiationReport1Builder obiePaymentInitiationReportBuilder;
//    private final OBIEPainXmlReport1Builder obiePainXmlReportBuilder;
//
//    public PaymentReportFile1Service(OBIEPaymentInitiationReport1Builder obiePaymentInitiationReportBuilder,
//                                     OBIEPainXmlReport1Builder obiePainXmlReportBuilder) {
//        this.obiePaymentInitiationReportBuilder = obiePaymentInitiationReportBuilder;
//        this.obiePainXmlReportBuilder = obiePainXmlReportBuilder;
//    }

    public String createPaymentReport(FRFilePaymentSubmission filePayment) throws OBErrorResponseException {
        log.debug("Create file payment report for filePayment: {}", filePayment.getId());

        // TODO - determine what functionality is required
        throw new UnsupportedOperationException("createPaymentReport not implemented");

//        Preconditions.checkNotNull(filePayment, "filePayment cannot be null");
//
//        String fileType = filePayment.getFilePayment().getData().getInitiation().getFileType();
//        switch (fileType) {
//            case UK_OBIE_PAYMENT_INITIATION_V3_0:
//                return obiePaymentInitiationReportBuilder.toPaymentReport(filePayment);
//            case UK_OBIE_PAIN_001:
//                return obiePainXmlReportBuilder.toPaymentReport(filePayment);
//            default:
//                log.error("File payment submitted with file type {} should not have passed validation. No report file is supported for this type.", fileType);
//                throw new IllegalArgumentException("Unknown payment file type: " + fileType);
//        }
    }
}
