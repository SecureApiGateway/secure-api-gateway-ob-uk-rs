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
package com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.processor.xml;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.common.FRAmountConverter.toFRAmount;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXB;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRFilePayment;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.DefaultPaymentFileType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.PaymentFile;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.jaxb.pain001.CreditTransferTransaction26;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.jaxb.pain001.CreditorReferenceInformation2;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.jaxb.pain001.Document;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.jaxb.pain001.PaymentInstruction22;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.jaxb.pain001.StructuredRemittanceInformation13;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.processor.BasePaymentFileProcessor;

import uk.org.openbanking.datamodel.v3.common.OBActiveOrHistoricCurrencyAndAmount;

@Component
public class OBIEPain001FileProcessor extends BasePaymentFileProcessor {

    public OBIEPain001FileProcessor() {
        super(DefaultPaymentFileType.UK_OBIE_PAIN_001.getPaymentFileType());
    }

    @Override
    protected PaymentFile processFileImpl(String fileContent) throws OBErrorException {
        try {
            logger.debug("Attempt to unmarshal XML '{}'", fileContent);
            Document document = JAXB.unmarshal(new StringReader(fileContent), Document.class);
            logger.debug("Unmarshalled to document");
            final List<PaymentInstruction22> pmtInf = document.getCstmrCdtTrfInitn().getPmtInf();
            final List<FRFilePayment> payments = new ArrayList<>();
            BigDecimal controlSum = BigDecimal.ZERO;
            for (final PaymentInstruction22 paymentInstruction22 : pmtInf) {
                for (final CreditTransferTransaction26 creditTransferTransaction26 : paymentInstruction22.getCdtTrfTxInf()) {
                    final FRFilePayment filePayment = toFRFilePayment(creditTransferTransaction26);
                    payments.add(filePayment);
                    controlSum = controlSum.add(new BigDecimal(filePayment.getInstructedAmount().getAmount()));
                }
            }
            return createPaymentFile(payments, controlSum);
        } catch (Exception e) {
            logger.warn("JAXB exception while attempting to unmarshal: {}", fileContent, e);
            throw new OBErrorException(OBRIErrorType.REQUEST_FILE_INVALID, e.getMessage());
        }
    }
    
    private FRFilePayment toFRFilePayment(CreditTransferTransaction26 payment) {
        String remittanceReference = payment
                .getRmtInf()
                .getStrd()
                .stream()
                .findAny()
                .map(StructuredRemittanceInformation13::getCdtrRefInf)
                .map(CreditorReferenceInformation2::getRef)
                .orElse("");
        String remittanceUnstructured = payment.getRmtInf().getUstrd().stream().findFirst().orElse("");

        String creditorAccountId = payment.getCdtrAcct().getId().getIBAN();
        if (StringUtils.isEmpty(creditorAccountId)) {
            creditorAccountId = payment.getCdtrAcct().getId().getOthr().getId();
        }

        OBActiveOrHistoricCurrencyAndAmount amt = new OBActiveOrHistoricCurrencyAndAmount()
                .amount(payment.getAmt().getInstdAmt().getValue().toPlainString())
                .currency(payment.getAmt().getInstdAmt().getCcy());

        return FRFilePayment.builder()
                .instructionIdentification(payment.getPmtId().getInstrId())
                .endToEndIdentification(payment.getPmtId().getEndToEndId())
                .instructedAmount(toFRAmount(amt))
                .created(DateTime.now())
                .creditorAccountIdentification(creditorAccountId)
                .remittanceReference(remittanceReference)
                .remittanceUnstructured(remittanceUnstructured)
                .status(FRFilePayment.PaymentStatus.PENDING)
                .build();
    }
}
