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
package com.forgerock.sapi.gateway.rs.resource.store.repo.entity.payment;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRExchangeRateInformation;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRSubmissionStatus;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternational;
import com.forgerock.sapi.gateway.rs.resource.store.datamodel.shared.currency.CurrencyRateService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import lombok.Builder;
import lombok.Data;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Data
@Document
public class FRInternationalPaymentSubmission implements PaymentSubmission<FRWriteInternational> {

    @Id
    @Indexed
    private String id;

    private FRWriteInternational payment;

    private FRSubmissionStatus status;

    @CreatedDate
    private DateTime created;
    @LastModifiedDate
    private DateTime updated;

    private String idempotencyKey;

    private OBVersion obVersion;

    /**
     * @return {@link FRExchangeRateInformation} with rate and expiry date fields populated where appropriate.
     */
    public FRExchangeRateInformation getCalculatedExchangeRate() {
        FRExchangeRateInformation exchangeRateInformation = payment.getData().getInitiation().getExchangeRateInformation();
        return CurrencyRateService.getCalculatedExchangeRate(exchangeRateInformation, created);
    }

    @Override
    public String getConsentId() {
        return payment.getData().getConsentId();
    }

}
