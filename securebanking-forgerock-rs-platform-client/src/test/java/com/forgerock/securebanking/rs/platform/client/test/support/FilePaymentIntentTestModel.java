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
package com.forgerock.securebanking.rs.platform.client.test.support;

import lombok.Builder;
import lombok.Data;
import uk.org.openbanking.datamodel.payment.OBWriteFileConsentResponse4Data;

import java.math.BigDecimal;

@Data
@Builder
public class FilePaymentIntentTestModel {
    private String consentId;
    private String fileContent;
    private String fileHash;
    private String fileReference;
    private String fileType;
    private String numberOfTransactions;
    private BigDecimal controlSum;
    private OBWriteFileConsentResponse4Data.StatusEnum status;
    private String resourceOwnerUsername;
    private String oauth2ClientId;
    private String accountId;
}