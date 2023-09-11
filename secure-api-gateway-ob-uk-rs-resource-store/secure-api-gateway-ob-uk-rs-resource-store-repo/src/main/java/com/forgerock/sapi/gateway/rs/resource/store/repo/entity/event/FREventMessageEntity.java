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
package com.forgerock.sapi.gateway.rs.resource.store.repo.entity.event;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.event.FREventPollingError;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * An event generated by an ASPSP that a third-party user of the sandbox may be interested in.
 * Examples include: payment processing and consent revocation.
 * Events are intended for occurrences that originate in the bank simulation or from a PSU. i.e. things the TPP would not be aware of.
 * Creation and authorisation of consents are performed by the TPP via REST and should not require an event as the TPP gets a REST response as part of the action.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class FREventMessageEntity implements Persistable<String> {

    @Id
    @Indexed
    private String id;

    @Indexed
    private String jti;

    private String set;

    @Indexed
    private String apiClientId;

    @CreatedDate
    private DateTime created;
    @LastModifiedDate
    private DateTime updated;

    private FREventPollingError errors;

    @Override
    public boolean isNew() {
        return id == null;
    }

    public boolean hasErrors() {
        return errors != null;
    }
}