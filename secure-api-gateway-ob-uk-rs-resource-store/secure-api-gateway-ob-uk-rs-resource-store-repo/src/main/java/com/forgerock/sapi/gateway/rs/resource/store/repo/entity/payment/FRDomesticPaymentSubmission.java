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
package com.forgerock.sapi.gateway.rs.resource.store.repo.entity.payment;

import java.util.Date;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRSubmissionStatus;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomestic;
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
public class FRDomesticPaymentSubmission implements PaymentSubmission<FRWriteDomestic> {

    @Id
    @Indexed
    private String id;

    private FRWriteDomestic payment;

    private FRSubmissionStatus status;

    @CreatedDate
    private Date created;
    @LastModifiedDate
    private Date updated;

    private String idempotencyKey;

    private OBVersion obVersion;

    @Override
    public String getConsentId() {
        return payment.getData().getConsentId();
    }
}
