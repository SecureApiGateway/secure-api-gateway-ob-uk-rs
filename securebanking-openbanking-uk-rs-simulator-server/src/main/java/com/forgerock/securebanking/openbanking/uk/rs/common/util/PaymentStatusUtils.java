package com.forgerock.securebanking.openbanking.uk.rs.common.util;

import java.util.Map;

/**
 * Added for operations and constants regarding the payment status for the payments APIs
 */
public class PaymentStatusUtils {

    public static final Map<String, String> statusLinkingMap = Map.of(
            "InitiationPending", "Pending",
            "InitiationFailed", "Rejected",
            "InitiationCompleted", "Accepted",
            "Cancelled", "Cancelled"
    );

}