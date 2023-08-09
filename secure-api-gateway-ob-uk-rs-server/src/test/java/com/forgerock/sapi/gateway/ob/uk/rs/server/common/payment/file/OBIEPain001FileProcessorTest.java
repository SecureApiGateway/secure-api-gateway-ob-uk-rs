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
package com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file;

import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.processor.OBIEPain001FileProcessor;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.processor.PaymentFileProcessor;

class OBIEPain001FileProcessorTest extends BasePaymentFileProcessorTest {

    @Override
    protected PaymentFileProcessor createFileProcessor() {
        return new OBIEPain001FileProcessor();
    }
}