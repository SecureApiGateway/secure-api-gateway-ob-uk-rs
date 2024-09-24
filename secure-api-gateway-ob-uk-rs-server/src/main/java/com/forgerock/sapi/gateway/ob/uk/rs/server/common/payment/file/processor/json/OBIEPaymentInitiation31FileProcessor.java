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
package com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.processor.json;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.common.FRAmountConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRFilePayment;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.DefaultPaymentFileType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.PaymentFile;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.processor.BasePaymentFileProcessor;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.processor.json.schema.OBDomestic2;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.processor.json.schema.OBRemittanceInformation1;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.JsonUtils;

@Component
public class OBIEPaymentInitiation31FileProcessor extends BasePaymentFileProcessor {

    private static final String DATA_NODE = "Data";
    private static final String DOMESTIC_PAYMENTS_NODE = "DomesticPayments";

    private final ObjectMapper objectMapper;

    public OBIEPaymentInitiation31FileProcessor() {
        super(DefaultPaymentFileType.UK_OBIE_PAYMENT_INITIATION_V3_1.getPaymentFileType());
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected PaymentFile processFileImpl(String fileContent) throws OBErrorException {
        try {
            final JsonNode dataNode = objectMapper.readTree(fileContent).path(DATA_NODE);

            final AtomicReference<BigDecimal> controlSumRef = new AtomicReference<>(BigDecimal.ZERO);
            final List<FRFilePayment> payments = JsonUtils.streamArray(dataNode.path(DOMESTIC_PAYMENTS_NODE))
                    .map(this::toOBDomestic)
                    .map(obDomestic2 -> {
                        final FRFilePayment frFilePayment = this.toFRFilePayment(obDomestic2);
                        final BigDecimal paymentAmount = new BigDecimal(frFilePayment.getInstructedAmount().getAmount());
                        controlSumRef.set(controlSumRef.get().add(paymentAmount));
                        return frFilePayment;
                    })
                    .collect(Collectors.toList());

            return createPaymentFile(payments, controlSumRef.get());
        } catch (Exception e) {
            logger.error("Error parsing JSON file. File content: '{}'", fileContent, e);
            throw new OBErrorException(OBRIErrorType.REQUEST_FILE_INVALID, e.getMessage());
        }
    }
    private OBDomestic2 toOBDomestic(JsonNode paymentNode) {
        try {
            return objectMapper.treeToValue(paymentNode, OBDomestic2.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private FRFilePayment toFRFilePayment(OBDomestic2 payment) {
        Optional<OBRemittanceInformation1> remittanceInformation = Optional.ofNullable(payment.getRemittanceInformation());
        return FRFilePayment.builder()
                .instructionIdentification(payment.getInstructionIdentification())
                .endToEndIdentification(payment.getEndToEndIdentification())
                .instructedAmount(FRAmountConverter.toFRAmount(payment.getInstructedAmount()))
                .created(DateTime.now())
                .creditorAccountIdentification(payment.getCreditorAccount().getIdentification())
                .remittanceReference(remittanceInformation.map(OBRemittanceInformation1::getReference).orElse(""))
                .remittanceUnstructured(remittanceInformation.map(OBRemittanceInformation1::getUnstructured).orElse(""))
                .status(FRFilePayment.PaymentStatus.PENDING)
                .build();
    }

}
