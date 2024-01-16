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
package com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRStandingOrderData;
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
public class FRStandingOrder {

    @Id
    private String id;
    @Indexed
    private String accountId;
    private FRStandingOrderData standingOrder;

    @Indexed
    private String pispId;

    @CreatedDate
    private DateTime created;
    @LastModifiedDate
    private DateTime updated;

    private String rejectionReason;

    private StandingOrderStatus status;

    /**
     * Internal status of a standing order with regards to payment execution
     */
    public enum StandingOrderStatus {
        // Waiting for first payment
        PENDING,
        // Made first payment but not yet made final payment
        ACTIVE,
        // Error in standing order prevented payment
        REJECTED,
        // Made final payment and no longer active
        COMPLETED
    }

}
