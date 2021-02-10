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

import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.payment.FRSubmissionStatus;
import uk.org.openbanking.datamodel.payment.*;

public class FRSubmissionStatusConverter {

    // FR to OB
    // v3.0 to 3.1.2 - Immediate Domestic/International Payments
    public static OBTransactionIndividualStatus1Code toOBTransactionIndividualStatus1Code(FRSubmissionStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case PENDING -> OBTransactionIndividualStatus1Code.PENDING;
            case ACCEPTEDSETTLEMENTINPROCESS -> OBTransactionIndividualStatus1Code.ACCEPTEDSETTLEMENTINPROCESS;
            case ACCEPTEDSETTLEMENTCOMPLETED,
                    ACCEPTEDCREDITSETTLEMENTCOMPLETED,
                    ACCEPTEDWITHOUTPOSTING -> OBTransactionIndividualStatus1Code.ACCEPTEDSETTLEMENTCOMPLETED;
            default -> OBTransactionIndividualStatus1Code.REJECTED;
        };
    }

    // v3.0 to 3.1.2 - Domestic/International Scheduled Payments or Standing Orders, or File Payments
    public static OBExternalStatus1Code toOBExternalStatus1Code(FRSubmissionStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case INITIATIONPENDING -> OBExternalStatus1Code.INITIATIONPENDING;
            case INITIATIONCOMPLETED -> OBExternalStatus1Code.INITIATIONCOMPLETED;
            default -> OBExternalStatus1Code.INITIATIONFAILED;
        };
    }

    // v3.1.3 - Immediate Domestic Payments
    public static OBWriteDomesticResponse3Data.StatusEnum toOBWriteDomesticResponse3DataStatus(FRSubmissionStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case PENDING -> OBWriteDomesticResponse3Data.StatusEnum.PENDING;
            case ACCEPTEDSETTLEMENTINPROCESS -> OBWriteDomesticResponse3Data.StatusEnum.ACCEPTEDSETTLEMENTINPROCESS;
            case ACCEPTEDSETTLEMENTCOMPLETED,
                    ACCEPTEDCREDITSETTLEMENTCOMPLETED,
                    ACCEPTEDWITHOUTPOSTING -> OBWriteDomesticResponse3Data.StatusEnum.ACCEPTEDSETTLEMENTCOMPLETED;
            default -> OBWriteDomesticResponse3Data.StatusEnum.REJECTED;
        };
    }

    // v3.1.4 - Immediate Domestic Payments
    public static OBWriteDomesticResponse4Data.StatusEnum toOBWriteDomesticResponse4DataStatus(FRSubmissionStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case PENDING -> OBWriteDomesticResponse4Data.StatusEnum.PENDING;
            case ACCEPTEDSETTLEMENTINPROCESS -> OBWriteDomesticResponse4Data.StatusEnum.ACCEPTEDSETTLEMENTINPROCESS;
            case ACCEPTEDSETTLEMENTCOMPLETED,
                    ACCEPTEDCREDITSETTLEMENTCOMPLETED,
                    ACCEPTEDWITHOUTPOSTING -> OBWriteDomesticResponse4Data.StatusEnum.ACCEPTEDSETTLEMENTCOMPLETED;
            default -> OBWriteDomesticResponse4Data.StatusEnum.REJECTED;
        };
    }

    // v3.1.3 - Domestic Scheduled Payments
    public static OBWriteDomesticScheduledResponse3Data.StatusEnum toOBWriteDomesticScheduledResponse3DataStatus(FRSubmissionStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case INITIATIONPENDING -> OBWriteDomesticScheduledResponse3Data.StatusEnum.INITIATIONPENDING;
            case INITIATIONCOMPLETED -> OBWriteDomesticScheduledResponse3Data.StatusEnum.INITIATIONCOMPLETED;
            case CANCELLED -> OBWriteDomesticScheduledResponse3Data.StatusEnum.CANCELLED;
            default -> OBWriteDomesticScheduledResponse3Data.StatusEnum.INITIATIONFAILED;
        };
    }

    // v3.1.4 - Domestic Scheduled Payments
    public static OBWriteDomesticScheduledResponse4Data.StatusEnum toOBWriteDomesticScheduledResponse4DataStatus(FRSubmissionStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case INITIATIONPENDING -> OBWriteDomesticScheduledResponse4Data.StatusEnum.INITIATIONPENDING;
            case INITIATIONCOMPLETED -> OBWriteDomesticScheduledResponse4Data.StatusEnum.INITIATIONCOMPLETED;
            case CANCELLED -> OBWriteDomesticScheduledResponse4Data.StatusEnum.CANCELLED;
            default -> OBWriteDomesticScheduledResponse4Data.StatusEnum.INITIATIONFAILED;
        };
    }

    // v3.1.3 - Domestic Standing Orders
    public static OBWriteDomesticStandingOrderResponse4Data.StatusEnum toOBWriteDomesticStandingOrderResponse4DataStatus(FRSubmissionStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case INITIATIONPENDING -> OBWriteDomesticStandingOrderResponse4Data.StatusEnum.INITIATIONPENDING;
            case INITIATIONCOMPLETED -> OBWriteDomesticStandingOrderResponse4Data.StatusEnum.INITIATIONCOMPLETED;
            case CANCELLED -> OBWriteDomesticStandingOrderResponse4Data.StatusEnum.CANCELLED;
            default -> OBWriteDomesticStandingOrderResponse4Data.StatusEnum.INITIATIONFAILED;
        };
    }

    // v3.1.4 - Domestic Standing Orders
    public static OBWriteDomesticStandingOrderResponse5Data.StatusEnum toOBWriteDomesticStandingOrderResponse5DataStatus(FRSubmissionStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case INITIATIONPENDING -> OBWriteDomesticStandingOrderResponse5Data.StatusEnum.INITIATIONPENDING;
            case INITIATIONCOMPLETED -> OBWriteDomesticStandingOrderResponse5Data.StatusEnum.INITIATIONCOMPLETED;
            case CANCELLED -> OBWriteDomesticStandingOrderResponse5Data.StatusEnum.CANCELLED;
            default -> OBWriteDomesticStandingOrderResponse5Data.StatusEnum.INITIATIONFAILED;
        };
    }

    // v3.1.3 onwards - Immediate International Payments
    public static OBWriteInternationalResponse4Data.StatusEnum toOBWriteInternationalResponse4DataStatus(FRSubmissionStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case PENDING -> OBWriteInternationalResponse4Data.StatusEnum.PENDING;
            case ACCEPTEDSETTLEMENTINPROCESS -> OBWriteInternationalResponse4Data.StatusEnum.ACCEPTEDSETTLEMENTINPROCESS;
            case ACCEPTEDSETTLEMENTCOMPLETED,
                    ACCEPTEDCREDITSETTLEMENTCOMPLETED,
                    ACCEPTEDWITHOUTPOSTING -> OBWriteInternationalResponse4Data.StatusEnum.ACCEPTEDSETTLEMENTCOMPLETED;
            default -> OBWriteInternationalResponse4Data.StatusEnum.REJECTED;
        };
    }

    // v3.1.3 - International Scheduled Payments
    public static OBWriteInternationalScheduledResponse4Data.StatusEnum toOBWriteInternationalScheduledResponse4DataStatus(FRSubmissionStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case INITIATIONPENDING -> OBWriteInternationalScheduledResponse4Data.StatusEnum.INITIATIONPENDING;
            case INITIATIONCOMPLETED -> OBWriteInternationalScheduledResponse4Data.StatusEnum.INITIATIONCOMPLETED;
            case CANCELLED -> OBWriteInternationalScheduledResponse4Data.StatusEnum.CANCELLED;
            default -> OBWriteInternationalScheduledResponse4Data.StatusEnum.INITIATIONFAILED;
        };
    }

    // v3.1.4 - International Scheduled Payments
    public static OBWriteInternationalScheduledResponse5Data.StatusEnum toOBWriteInternationalScheduledResponse5DataStatus(FRSubmissionStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case INITIATIONPENDING -> OBWriteInternationalScheduledResponse5Data.StatusEnum.INITIATIONPENDING;
            case INITIATIONCOMPLETED -> OBWriteInternationalScheduledResponse5Data.StatusEnum.INITIATIONCOMPLETED;
            case CANCELLED -> OBWriteInternationalScheduledResponse5Data.StatusEnum.CANCELLED;
            default -> OBWriteInternationalScheduledResponse5Data.StatusEnum.INITIATIONFAILED;
        };
    }

    // v3.1.3 - International Standing Orders
    public static OBWriteInternationalStandingOrderResponse5Data.StatusEnum toOBWriteInternationalStandingOrderResponse5DataStatus(FRSubmissionStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case INITIATIONPENDING -> OBWriteInternationalStandingOrderResponse5Data.StatusEnum.INITIATIONPENDING;
            case INITIATIONCOMPLETED -> OBWriteInternationalStandingOrderResponse5Data.StatusEnum.INITIATIONCOMPLETED;
            case CANCELLED -> OBWriteInternationalStandingOrderResponse5Data.StatusEnum.CANCELLED;
            default -> OBWriteInternationalStandingOrderResponse5Data.StatusEnum.INITIATIONFAILED;
        };
    }

    // v3.1.4 - International Standing Orders
    public static OBWriteInternationalStandingOrderResponse6Data.StatusEnum toOBWriteInternationalStandingOrderResponse6DataStatus(FRSubmissionStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case INITIATIONPENDING -> OBWriteInternationalStandingOrderResponse6Data.StatusEnum.INITIATIONPENDING;
            case INITIATIONCOMPLETED -> OBWriteInternationalStandingOrderResponse6Data.StatusEnum.INITIATIONCOMPLETED;
            case CANCELLED -> OBWriteInternationalStandingOrderResponse6Data.StatusEnum.CANCELLED;
            default -> OBWriteInternationalStandingOrderResponse6Data.StatusEnum.INITIATIONFAILED;
        };
    }
}
