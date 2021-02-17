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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRDataAuthorisation;
import uk.org.openbanking.datamodel.payment.OBAuthorisation1;
import uk.org.openbanking.datamodel.payment.OBExternalAuthorisation1Code;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsent3DataAuthorisation;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsent4DataAuthorisation;

public class FRDataAuthorisationConverter {

    // OB to FR
    public static FRDataAuthorisation toFRDataAuthorisation(OBAuthorisation1 authorisation) {
        return authorisation == null ? null : FRDataAuthorisation.builder()
                .authorisationType(toFRAuthorisationType(authorisation.getAuthorisationType()))
                .completionDateTime(authorisation.getCompletionDateTime())
                .build();
    }

    public static FRDataAuthorisation toFRDataAuthorisation(OBWriteDomesticConsent3DataAuthorisation authorisation) {
        return authorisation == null ? null : FRDataAuthorisation.builder()
                .authorisationType(toFRAuthorisationType(authorisation.getAuthorisationType()))
                .completionDateTime(authorisation.getCompletionDateTime())
                .build();
    }

    public static FRDataAuthorisation toFRDataAuthorisation(OBWriteDomesticConsent4DataAuthorisation authorisation) {
        return authorisation == null ? null : FRDataAuthorisation.builder()
                .authorisationType(toFRAuthorisationType(authorisation.getAuthorisationType()))
                .completionDateTime(authorisation.getCompletionDateTime())
                .build();
    }

    public static FRDataAuthorisation.AuthorisationType toFRAuthorisationType(OBExternalAuthorisation1Code authorisationType) {
        return authorisationType == null ? null : FRDataAuthorisation.AuthorisationType.valueOf(authorisationType.name());
    }

    public static FRDataAuthorisation.AuthorisationType toFRAuthorisationType(OBWriteDomesticConsent3DataAuthorisation.AuthorisationTypeEnum authorisationType) {
        return authorisationType == null ? null : FRDataAuthorisation.AuthorisationType.valueOf(authorisationType.name());
    }

    public static FRDataAuthorisation.AuthorisationType toFRAuthorisationType(OBWriteDomesticConsent4DataAuthorisation.AuthorisationTypeEnum authorisationType) {
        return authorisationType == null ? null : FRDataAuthorisation.AuthorisationType.valueOf(authorisationType.name());
    }

    // FR to OB
    public static OBWriteDomesticConsent3DataAuthorisation toOBWriteDomesticConsent3DataAuthorisation(FRDataAuthorisation authorisation) {
        return authorisation == null ? null : new OBWriteDomesticConsent3DataAuthorisation()
                .authorisationType(toOBWriteDomesticConsent3DataAuthorisationType(authorisation.getAuthorisationType()))
                .completionDateTime(authorisation.getCompletionDateTime());
    }

    public static OBWriteDomesticConsent4DataAuthorisation toOBWriteDomesticConsent4DataAuthorisation(FRDataAuthorisation authorisation) {
        return authorisation == null ? null : new OBWriteDomesticConsent4DataAuthorisation()
                .authorisationType(toOBWriteDomesticConsent4DataAuthorisationType(authorisation.getAuthorisationType()))
                .completionDateTime(authorisation.getCompletionDateTime());
    }

    public static OBAuthorisation1 toOBAuthorisation1(FRDataAuthorisation authorisation) {
        return authorisation == null ? null : new OBAuthorisation1()
                .authorisationType(toOBExternalAuthorisation1Code(authorisation.getAuthorisationType()))
                .completionDateTime(authorisation.getCompletionDateTime());
    }

    public static OBWriteDomesticConsent3DataAuthorisation.AuthorisationTypeEnum toOBWriteDomesticConsent3DataAuthorisationType(FRDataAuthorisation.AuthorisationType authorisationType) {
        return authorisationType == null ? null : OBWriteDomesticConsent3DataAuthorisation.AuthorisationTypeEnum.valueOf(authorisationType.name());
    }

    public static OBWriteDomesticConsent4DataAuthorisation.AuthorisationTypeEnum toOBWriteDomesticConsent4DataAuthorisationType(FRDataAuthorisation.AuthorisationType authorisationType) {
        return authorisationType == null ? null : OBWriteDomesticConsent4DataAuthorisation.AuthorisationTypeEnum.valueOf(authorisationType.name());
    }

    public static OBExternalAuthorisation1Code toOBExternalAuthorisation1Code(FRDataAuthorisation.AuthorisationType authorisationType) {
        return authorisationType == null ? null : OBExternalAuthorisation1Code.valueOf(authorisationType.name());
    }
}
