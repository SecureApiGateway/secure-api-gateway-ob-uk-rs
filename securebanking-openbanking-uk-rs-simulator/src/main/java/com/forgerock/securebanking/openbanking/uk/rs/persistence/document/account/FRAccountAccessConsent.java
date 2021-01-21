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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.forgerock.securebanking.openbanking.uk.rs.common.version.OBVersion;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRReadConsentResponse;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRExternalRequestStatusCode;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FRAccountAccessConsent implements AccountRequest, Persistable<String> {
    @Id
    @Indexed
    public String id;
    private FRReadConsentResponse accountAccessConsent;
    @Indexed
    private String clientId;
    private String aispId;
    private String aispName;
    private String consentId;
    private List<String> accountIds = new ArrayList<>();
    private String userId;

    @CreatedDate
    public Date created;
    @LastModifiedDate
    public Date updated;

    OBVersion obVersion;

    @Override
    public boolean isNew() {
        return created == null;
    }

    public void setAisp(String clientId, String aispId, String aispName) {
        this.clientId = clientId;
        this.aispId = aispId;
        this.aispName = aispName;
    }

    @Override
    @JsonIgnore
    public FRExternalRequestStatusCode getStatus() {
        return accountAccessConsent.getData().getStatus();
    }

    @Override
    @JsonIgnore
    public void setStatus(FRExternalRequestStatusCode code) {
        getAccountAccessConsent().getData().setStatus(code);
    }

    @Override
    @JsonIgnore
    public List<FRExternalPermissionsCode> getPermissions() {
        return getAccountAccessConsent().getData().getPermissions();
    }

    @Override
    @JsonIgnore
    public DateTime getExpirationDateTime() {
        return getAccountAccessConsent().getData().getExpirationDateTime();
    }

    @Override
    @JsonIgnore
    public DateTime getTransactionFromDateTime() {
        return getAccountAccessConsent().getData().getTransactionFromDateTime();
    }

    @Override
    @JsonIgnore
    public DateTime getTransactionToDateTime() {
        return getAccountAccessConsent().getData().getTransactionToDateTime();
    }

}
