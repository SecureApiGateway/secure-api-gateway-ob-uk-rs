/**
 * Copyright © 2020 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRScheduledPaymentData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Representation of an account. This model is only useful for the demo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class FRScheduledPayment {

    @Id
    private String id;
    @Indexed
    private String accountId;
    private FRScheduledPaymentData scheduledPayment;

    @Indexed
    private String pispId;

    @CreatedDate
    private DateTime created;
    @LastModifiedDate
    private DateTime updated;

    private String rejectionReason;

    private ScheduledPaymentStatus status;

    /** Records if this scheduled payment has been processed or not. Not all scheduled payments have an associated payment consent (e.g. created on /data API). */
    public enum ScheduledPaymentStatus {
        PENDING,
        COMPLETED,
        REJECTED
    }
}
