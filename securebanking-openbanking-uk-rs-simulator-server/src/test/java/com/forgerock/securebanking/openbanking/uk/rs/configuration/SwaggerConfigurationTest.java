package com.forgerock.securebanking.openbanking.uk.rs.configuration;

import com.forgerock.securebanking.openbanking.uk.rs.configuration.swagger.SwaggerConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link SwaggerConfiguration}
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SwaggerConfiguration.class, initializers = ConfigFileApplicationContextInitializer.class)
@EnableConfigurationProperties(value = SwaggerConfiguration.class)
@ActiveProfiles("test")
public class SwaggerConfigurationTest {

    @Autowired
    private SwaggerConfiguration swaggerConfiguration;

    @Test
    public void haveProperties(){
        assertThat(swaggerConfiguration.getTitle()).isNotNull();
        assertThat(swaggerConfiguration.getDescription()).isNotNull();
        assertThat(swaggerConfiguration.getLicense()).isNotNull();
        assertThat(swaggerConfiguration.getLicenseUrl()).isNotNull();
        assertThat(swaggerConfiguration.getTermsOfServiceUrl()).isNotNull();
        assertThat(swaggerConfiguration.getContactName()).isNotNull();
        assertThat(swaggerConfiguration.getContactUrl()).isNotNull();
        assertThat(swaggerConfiguration.getDocketApisBasePackage()).isNotNull();
        assertThat(swaggerConfiguration.getDocketPathsSelectorRegex()).isNotNull();
    }
}
