package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.List;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRReadConsent;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRReadConsentData;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.account.consent.AccountResourceAccessService;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

/**
 * TestHelper methods for tests which have a dependency on the {@link AccountResourceAccessService}.
 */
public class AccountResourceAccessServiceTestHelpers {

    public static AccountAccessConsent createAuthorisedConsentAllPermissions(String... accountIds) {
        return createAuthorisedConsent(Arrays.asList(FRExternalPermissionsCode.values()), accountIds);
    }

    public static AccountAccessConsent createAuthorisedConsent(List<FRExternalPermissionsCode> permissions, String... accountIds) {
        final AccountAccessConsent consent = new AccountAccessConsent();
        consent.setId(IntentType.ACCOUNT_ACCESS_CONSENT.generateIntentId());
        consent.setApiClientId("test-api-client-1");
        consent.setStatus("Authorised");
        consent.setRequestObj(FRReadConsent.builder().data(FRReadConsentData.builder().permissions(permissions).build()).build());
        consent.setAuthorisedAccountIds(Arrays.asList(accountIds));
        return consent;
    }

    public static void mockAccountResourceAccessServiceResponse(AccountResourceAccessService mockService, AccountAccessConsent consent) {
        try {
            given(mockService.getConsentForResourceAccess(eq(consent.getId()), eq(consent.getApiClientId()))).willReturn(consent);
        } catch (OBErrorException e) {
            throw new RuntimeException(e);
        }
    }

    public static void mockAccountResourceAccessServiceResponse(AccountResourceAccessService mockService, AccountAccessConsent consent, String... requestedAccountIds) {
        try {
            given(mockService.getConsentForResourceAccess(eq(consent.getId()), eq(consent.getApiClientId()), eq(Arrays.asList(requestedAccountIds)))).willReturn(consent);
        } catch (OBErrorException e) {
            throw new RuntimeException(e);
        }
    }

}
