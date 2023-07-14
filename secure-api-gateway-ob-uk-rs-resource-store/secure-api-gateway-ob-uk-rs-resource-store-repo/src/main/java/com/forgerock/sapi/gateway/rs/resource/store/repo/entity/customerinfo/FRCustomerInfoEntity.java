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
package com.forgerock.sapi.gateway.rs.resource.store.repo.entity.customerinfo;

import com.forgerock.sapi.gateway.rs.resource.store.datamodel.customerinfo.FRCustomerInfo;
import com.forgerock.sapi.gateway.rs.resource.store.datamodel.customerinfo.FRCustomerInfoAddress;
import lombok.Data;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;


@Data
@Document
public class FRCustomerInfoEntity {

    public FRCustomerInfoEntity(FRCustomerInfo customerInfo) {
        this.partyId = customerInfo.getPartyId();
        this.title = customerInfo.getPartyId();
        this.initials = customerInfo.getInitials();
        this.familyName = customerInfo.getFamilyName();
        this.givenName = customerInfo.getGivenName();
        this.email = customerInfo.getEmail();
        this.phoneNumber = customerInfo.getPhoneNumber();
        this.birthdate = customerInfo.getBirthdate();
        this.address = customerInfo.getAddress();
    }

    @Id
    @Indexed
    public String id;

    @Indexed
    public String userID;
    private String partyId;
    private String title;
    private String initials;
    private String familyName;
    private String givenName;
    private String email;
    private String phoneNumber;
    @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
    )
    private LocalDate birthdate;
    private FRCustomerInfoAddress address;

    @CreatedDate
    private DateTime created;
    @LastModifiedDate
    private DateTime updated;

    public FRCustomerInfo toFRCustomerInfo() {
        return FRCustomerInfo.builder()
                .id(this.id)
                .partyId(this.partyId)
                .title(this.title)
                .initials(this.initials)
                .familyName(this.familyName)
                .givenName(this.givenName)
                .email(this.email)
                .phoneNumber(this.phoneNumber)
                .birthdate(this.birthdate)
                .address(this.address)
                .build();
    }
}
