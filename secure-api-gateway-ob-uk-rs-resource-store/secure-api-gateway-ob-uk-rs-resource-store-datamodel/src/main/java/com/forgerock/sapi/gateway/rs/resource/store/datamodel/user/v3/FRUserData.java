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
package com.forgerock.sapi.gateway.rs.resource.store.datamodel.user.v3;

import com.forgerock.sapi.gateway.rs.resource.store.datamodel.account.v3.FRAccountData;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.customerinfo.FRCustomerInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.org.openbanking.datamodel.v3.account.OBParty2;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FRUserData {

    private String userName;
    private String userId;
    private OBParty2 party;
    private List<FRAccountData> accountDatas = new ArrayList<>();
    private FRCustomerInfo customerInfo;

    public FRUserData(String userId) {
        this.userId = userId;
    }

    public void addAccountData(FRAccountData accountData) {
        accountDatas.add(accountData);
    }
}
