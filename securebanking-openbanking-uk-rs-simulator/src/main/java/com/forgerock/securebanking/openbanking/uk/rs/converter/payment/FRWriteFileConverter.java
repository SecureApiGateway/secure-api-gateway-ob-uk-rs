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
package com.forgerock.securebanking.openbanking.uk.rs.converter.payment;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDataFile;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteFile;
import uk.org.openbanking.datamodel.payment.OBWriteDataFile1;
import uk.org.openbanking.datamodel.payment.OBWriteDataFile2;
import uk.org.openbanking.datamodel.payment.OBWriteFile1;
import uk.org.openbanking.datamodel.payment.OBWriteFile2;

public class FRWriteFileConverter {

    // OB to FR
    public static FRWriteFile toFRWriteFile(OBWriteFile1 obWriteFile1) {
        return obWriteFile1 == null ? null : FRWriteFile.builder()
                .data(toFRWriteDataFile(obWriteFile1.getData()))
                .build();
    }

    public static FRWriteFile toFRWriteFile(OBWriteFile2 obWriteFile2) {
        return obWriteFile2 == null ? null : FRWriteFile.builder()
                .data(toFRWriteDataFile(obWriteFile2.getData()))
                .build();
    }

    public static FRWriteDataFile toFRWriteDataFile(OBWriteDataFile1 data) {
        return data == null ? null : FRWriteDataFile.builder()
                .consentId(data.getConsentId())
                .initiation(FRWriteFileConsentConverter.toFRWriteFileDataInitiation(data.getInitiation()))
                .build();
    }

    public static FRWriteDataFile toFRWriteDataFile(OBWriteDataFile2 data) {
        return data == null ? null : FRWriteDataFile.builder()
                .consentId(data.getConsentId())
                .initiation(FRWriteFileConsentConverter.toFRWriteFileDataInitiation(data.getInitiation()))
                .build();
    }

    // FR to OB
    public static OBWriteFile2 toOBWriteFile2(FRWriteFile filePayment) {
        return filePayment == null ? null : new OBWriteFile2()
                .data(toOBWriteDataFile2(filePayment.getData()));
    }

    public static OBWriteDataFile2 toOBWriteDataFile2(FRWriteDataFile data) {
        return data == null ? null : new OBWriteDataFile2()
                .consentId(data.getConsentId())
                .initiation(FRWriteFileConsentConverter.toOBFile2(data.getInitiation()));
    }
}
