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

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.org.openbanking.datamodel.v4.account.OBReadProduct2DataProductInner;

/**
 * Representation of an account. This model is only useful for the demo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class FRProduct {

    @Id
    private String id;
    @Indexed
    private String accountId;
    // TODO - ideally we'd have our own domain model equivalent here to help shield the application from OB API changes. Unfortunately, there's an
    //  extensive hierarchy of classes under OBReadProduct2DataProduct, which will take time to model and convert, and isn't essential for an
    //  object that hasn't changed since v2.0 of the API. This is something we can come back to if time allows in the future.
    private OBReadProduct2DataProductInner product;

    @CreatedDate
    private Date created;
    @LastModifiedDate
    private Date updated;
}
