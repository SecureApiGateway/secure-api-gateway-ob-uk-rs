package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_11.beneficiaries;

import java.util.Optional;

import javax.annotation.Generated;
import javax.validation.constraints.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen")
@Controller
@RequestMapping("${openapi.accountAndTransactionAPISpecification.base-path:/open-banking/v3.1/aisp}")
public class BeneficiariesApiController implements BeneficiariesApi {

    private final NativeWebRequest request;

    @Autowired
    public BeneficiariesApiController(NativeWebRequest request) {
        this.request = request;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

}
