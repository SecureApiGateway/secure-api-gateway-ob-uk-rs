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
package com.forgerock.sapi.gateway.ob.uk.rs.server.service.scheduledpayment;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRScheduledPaymentData;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRScheduledPayment;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.scheduledpayments.FRScheduledPaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for saving, retrieving and updating {@link FRScheduledPayment FRScheduledPayments} for the Accounts API.
 */
@Service
@Slf4j
public class ScheduledPaymentService {

    private final FRScheduledPaymentRepository scheduledPaymentRepository;

    public ScheduledPaymentService(FRScheduledPaymentRepository scheduledPaymentRepository) {
        this.scheduledPaymentRepository = scheduledPaymentRepository;
    }

    /**
     * Saves a {@link FRScheduledPayment} within the repository.
     *
     * @param scheduledPaymentData The {@link FRScheduledPaymentData} containing the required payment information.
     * @return The persisted {@link FRScheduledPayment}.
     */
    public FRScheduledPayment createScheduledPayment(FRScheduledPaymentData scheduledPaymentData) {
        log.debug("Create a scheduled payment in the repository: {}", scheduledPaymentData);

        FRScheduledPayment frScheduledPayment = FRScheduledPayment.builder()
                .id(UUID.randomUUID().toString())
                .scheduledPayment(scheduledPaymentData)
                .accountId(scheduledPaymentData.getAccountId())
                .status(FRScheduledPayment.ScheduledPaymentStatus.PENDING)
                // TODO - do we need to persist the pispId?
                //.pispId(pispId)
                .build();
        return scheduledPaymentRepository.save(frScheduledPayment);
    }
}
