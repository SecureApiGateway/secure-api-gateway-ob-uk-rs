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
package com.forgerock.sapi.gateway.ob.uk.rs.server.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.org.openbanking.datamodel.account.*;

import java.util.Objects;
import java.util.function.Consumer;

@Service
@Slf4j
public class AccountDataInternalIdFilter {
    private final boolean showAccountDataInternalIds;

    public AccountDataInternalIdFilter(@Value("${rs.data.internal_ids.show:true}") boolean showAccountDataInternalIds) {
        this.showAccountDataInternalIds = showAccountDataInternalIds;
    }

    public OBTransaction6 apply(final OBTransaction6 data) {
        return apply(data, data::setTransactionId);
    }

    public OBBeneficiary5 apply(final OBBeneficiary5 data) {
        return apply(data, data::setBeneficiaryId);
    }

    public OBReadDirectDebit2DataDirectDebitInner apply(OBReadDirectDebit2DataDirectDebitInner data) {
        return apply(data, data::setDirectDebitId);
    }

    public OBReadOffer1DataOfferInner apply(OBReadOffer1DataOfferInner data) {
        return apply(data, data::setOfferId);
    }

    public OBReadProduct2DataProductInner apply(OBReadProduct2DataProductInner data) {
        return apply(data, data::setProductId);
    }

    public OBScheduledPayment3 apply(OBScheduledPayment3 data) {
        return apply(data, data::setScheduledPaymentId);
    }

    public OBStandingOrder6 apply(OBStandingOrder6 data) {
        return apply(data, data::setStandingOrderId);
    }

    public OBStatement2 apply(OBStatement2 data) {
        return apply(data, data::setStatementId);
    }

    private <T> T apply(final T data, Consumer<String> setIdFunction) {
        if (showAccountDataInternalIds) {
            log.debug("Show Account Data Internal Ids is 'ON'. Data response will contain internal ids");
            return data;
        }
        log.debug("Show Data API Internal Ids is 'OFF'. Data response will NOT contain internal ids. Data: {}", data);
        if (Objects.nonNull(data)) {
            setIdFunction.accept(null);
            log.debug("Removed id");
        }
        return data;
    }
}
