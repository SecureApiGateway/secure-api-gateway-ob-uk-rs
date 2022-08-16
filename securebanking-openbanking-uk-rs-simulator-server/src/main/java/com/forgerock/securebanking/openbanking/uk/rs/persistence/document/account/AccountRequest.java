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
package com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRExternalRequestStatusCode;
import org.joda.time.DateTime;

import java.util.List;

public interface AccountRequest {

    String getClientId();

    String getUserId();

    String getAispId();

    String getAispName();

    List<FRExternalPermissionsCode> getPermissions();

    DateTime getExpirationDateTime();

    DateTime getTransactionFromDateTime();

    DateTime getTransactionToDateTime();

    void setUserId(String userId);

    void setAccountIds(List<String> accountIds);

    void setStatus(FRExternalRequestStatusCode code);

    String getId();

    List<String> getAccountIds();

    FRExternalRequestStatusCode getStatus();
}
