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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRDataSCASupportData;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsent3DataSCASupportData;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsent4DataSCASupportData;

public class FRDataSCASupportDataConverter {

    // OB to FR
    public static FRDataSCASupportData toFRDataSCASupportData(OBWriteDomesticConsent3DataSCASupportData scASupportData) {
        return scASupportData == null ? null : FRDataSCASupportData.builder()
                .requestedSCAExemptionType(toFRRequestedSCAExemptionType(scASupportData.getRequestedSCAExemptionType()))
                .appliedAuthenticationApproach(toFRAppliedAuthenticationApproach(scASupportData.getAppliedAuthenticationApproach()))
                .referencePaymentOrderId(scASupportData.getReferencePaymentOrderId())
                .build();
    }

    public static FRDataSCASupportData toFRDataSCASupportData(OBWriteDomesticConsent4DataSCASupportData scASupportData) {
        return scASupportData == null ? null : FRDataSCASupportData.builder()
                .requestedSCAExemptionType(toFRRequestedSCAExemptionType(scASupportData.getRequestedSCAExemptionType()))
                .appliedAuthenticationApproach(toFRAppliedAuthenticationApproach(scASupportData.getAppliedAuthenticationApproach()))
                .referencePaymentOrderId(scASupportData.getReferencePaymentOrderId())
                .build();
    }

    public static FRDataSCASupportData.FRAppliedAuthenticationApproach toFRAppliedAuthenticationApproach(OBWriteDomesticConsent3DataSCASupportData.AppliedAuthenticationApproachEnum appliedAuthenticationApproach) {
        return appliedAuthenticationApproach == null ? null : FRDataSCASupportData.FRAppliedAuthenticationApproach.valueOf(appliedAuthenticationApproach.name());
    }

    public static FRDataSCASupportData.FRAppliedAuthenticationApproach toFRAppliedAuthenticationApproach(OBWriteDomesticConsent4DataSCASupportData.AppliedAuthenticationApproachEnum appliedAuthenticationApproach) {
        return appliedAuthenticationApproach == null ? null : FRDataSCASupportData.FRAppliedAuthenticationApproach.valueOf(appliedAuthenticationApproach.name());
    }

    public static FRDataSCASupportData.FRRequestedSCAExemptionType toFRRequestedSCAExemptionType(OBWriteDomesticConsent3DataSCASupportData.RequestedSCAExemptionTypeEnum requestedSCAExemptionType) {
        return requestedSCAExemptionType == null ? null : FRDataSCASupportData.FRRequestedSCAExemptionType.valueOf(requestedSCAExemptionType.name());
    }

    public static FRDataSCASupportData.FRRequestedSCAExemptionType toFRRequestedSCAExemptionType(OBWriteDomesticConsent4DataSCASupportData.RequestedSCAExemptionTypeEnum requestedSCAExemptionType) {
        return requestedSCAExemptionType == null ? null : FRDataSCASupportData.FRRequestedSCAExemptionType.valueOf(requestedSCAExemptionType.name());
    }

    // FR to OB
    public static OBWriteDomesticConsent3DataSCASupportData toOBWriteDomesticConsent3DataSCASupportData(FRDataSCASupportData scASupportData) {
        return scASupportData == null ? null : new OBWriteDomesticConsent3DataSCASupportData()
                .requestedSCAExemptionType(to3DataRequestedSCAExemptionType(scASupportData.getRequestedSCAExemptionType()))
                .appliedAuthenticationApproach(to3DataAppliedAuthenticationApproach(scASupportData.getAppliedAuthenticationApproach()))
                .referencePaymentOrderId(scASupportData.getReferencePaymentOrderId());
    }

    public static OBWriteDomesticConsent4DataSCASupportData toOBWriteDomesticConsent4DataSCASupportData(FRDataSCASupportData scASupportData) {
        return scASupportData == null ? null : new OBWriteDomesticConsent4DataSCASupportData()
                .requestedSCAExemptionType(to4DataRequestedSCAExemptionType(scASupportData.getRequestedSCAExemptionType()))
                .appliedAuthenticationApproach(to4DataAppliedAuthenticationApproach(scASupportData.getAppliedAuthenticationApproach()))
                .referencePaymentOrderId(scASupportData.getReferencePaymentOrderId());
    }

    public static OBWriteDomesticConsent3DataSCASupportData.RequestedSCAExemptionTypeEnum to3DataRequestedSCAExemptionType(FRDataSCASupportData.FRRequestedSCAExemptionType requestedSCAExemptionType) {
        return requestedSCAExemptionType == null ? null : OBWriteDomesticConsent3DataSCASupportData.RequestedSCAExemptionTypeEnum.valueOf(requestedSCAExemptionType.name());
    }

    public static OBWriteDomesticConsent4DataSCASupportData.RequestedSCAExemptionTypeEnum to4DataRequestedSCAExemptionType(FRDataSCASupportData.FRRequestedSCAExemptionType requestedSCAExemptionType) {
        return requestedSCAExemptionType == null ? null : OBWriteDomesticConsent4DataSCASupportData.RequestedSCAExemptionTypeEnum.valueOf(requestedSCAExemptionType.name());
    }

    public static OBWriteDomesticConsent3DataSCASupportData.AppliedAuthenticationApproachEnum to3DataAppliedAuthenticationApproach(FRDataSCASupportData.FRAppliedAuthenticationApproach appliedAuthenticationApproach) {
        return appliedAuthenticationApproach == null ? null : OBWriteDomesticConsent3DataSCASupportData.AppliedAuthenticationApproachEnum.valueOf(appliedAuthenticationApproach.name());
    }

    public static OBWriteDomesticConsent4DataSCASupportData.AppliedAuthenticationApproachEnum to4DataAppliedAuthenticationApproach(FRDataSCASupportData.FRAppliedAuthenticationApproach appliedAuthenticationApproach) {
        return appliedAuthenticationApproach == null ? null : OBWriteDomesticConsent4DataSCASupportData.AppliedAuthenticationApproachEnum.valueOf(appliedAuthenticationApproach.name());
    }
}
