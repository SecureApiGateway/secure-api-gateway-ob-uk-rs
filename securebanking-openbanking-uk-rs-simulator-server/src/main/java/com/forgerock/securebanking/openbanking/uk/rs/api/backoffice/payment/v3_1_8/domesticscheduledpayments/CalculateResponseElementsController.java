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
package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.v3_1_8.domesticscheduledpayments;

import com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.calculate.elements.v3_1_8.domesticscheduledpayments.CalculateResponseElements;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;


@RestController("CalculateDomesticScheduledPaymentsResponseElements_v3.1.8")
@Slf4j
public class CalculateResponseElementsController
        extends com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.v3_1_7.domesticscheduledpayments.CalculateResponseElementsController
        implements CalculateResponseElements {
}
