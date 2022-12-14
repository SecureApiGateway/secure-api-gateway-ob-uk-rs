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
package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.validations.v3_1_7.filepayments;

import com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.swagger.SwaggerApiTags;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;

@Api(tags = {"v3.1.7", SwaggerApiTags.BACKOFFICE})
@RequestMapping("/backoffice/v3.1.7/file-payment-consent")
public interface FilePaymentFileValidationsApi
        extends com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.validations.v3_1_6.filepayments.FilePaymentFileValidationsApi {
}