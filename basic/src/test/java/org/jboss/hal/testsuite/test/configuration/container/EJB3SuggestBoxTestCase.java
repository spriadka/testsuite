package org.jboss.hal.testsuite.test.configuration.container;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.PicketBox;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.page.config.EJB3Page;
import org.jboss.hal.testsuite.util.SuggestionChecker;
import org.jboss.hal.testsuite.util.SuggestionResource;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;

import java.io.IOException;

/**
 * This test case covers sugest box popups in ejb3 subsystem configuration
 *
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 8/25/16.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class EJB3SuggestBoxTestCase {

    @Drone
    WebDriver browser;

    @Page
    EJB3Page page;

    private final String DEFAULT_ENTITY_INSTANCE_POOL = "default-entity-bean-instance-pool";
    private final String DEFAULT_SECURITY_DOMAIN = "default-security-domain";

    private final String SECURITY_DOMAIN_FAIL_MESSAGE = "Probably failed because of https://issues.jboss.org/browse/HAL-1358.";

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();

    private final SuggestionResource securityDomainSuggestionResource = new SuggestionResource.Builder()
            .template(Address.subsystem("security").and("security-domain", "*"))
            .withClient(client)
            .build();

    private SuggestionChecker securityDomainSuggestionChecker;

    private SuggestionChecker getSecurityDomainSuggestionChecker() {
        if (securityDomainSuggestionChecker == null) {
            securityDomainSuggestionChecker = new SuggestionChecker.Builder()
                    .browser(browser)
                    .suggestionResource(securityDomainSuggestionResource)
                    .configFragment(page.getConfigFragment())
                    .suggestBoxInputLabel(DEFAULT_SECURITY_DOMAIN)
                    .build();
        }
        return securityDomainSuggestionChecker;
    }

    private final SuggestionResource instancePoolSuggestionResource = new SuggestionResource.Builder()
        .template(Address.subsystem("ejb3").and("strict-max-bean-instance-pool", "*"))
        .withClient(client)
        .build();

    private SuggestionChecker instancePoolSuggestionChecker;

    private SuggestionChecker getInstancePoolSuggestionChecker() {
        if (instancePoolSuggestionChecker == null) {
            instancePoolSuggestionChecker = new SuggestionChecker.Builder()
                    .browser(browser)
                    .suggestionResource(instancePoolSuggestionResource)
                    .configFragment(page.getConfigFragment())
                    .suggestBoxInputLabel(DEFAULT_ENTITY_INSTANCE_POOL)
                    .build();
        }
        return instancePoolSuggestionChecker;
    }

    @Test
    public void defaultEntityInstancePoolFilterStrict() throws IOException {
        page.navigate();
        getInstancePoolSuggestionChecker()
                .filterSuggestions("strict")
                .verifyOnlyRelevantSuggestionWereSuggested();
    }

    @Test
    public void defaultEntityInstancePoolFilter_EMPTY() throws IOException {
        page.navigate();
        getInstancePoolSuggestionChecker()
                .filterSuggestions("")
                .verifyOnlyRelevantSuggestionWereSuggested();
    }

    @Test
    public void defaultEntityInstancePoolFilter_slsb() throws IOException {
        page.navigate();
        getInstancePoolSuggestionChecker()
                .filterSuggestions("slsb")
                .verifyOnlyRelevantSuggestionWereSuggested();
    }

    @Test
    public void defaultEntityInstancePoolFilter_ejb3() throws IOException {
        page.navigate();
        getInstancePoolSuggestionChecker()
                .filterSuggestions("ejb3")
                .verifyOnlyRelevantSuggestionWereSuggested();
    }

    @Test
    public void defaultEntityInstancePoolFilter_str() throws IOException {
        page.navigate();
        getInstancePoolSuggestionChecker()
                .filterSuggestions("str")
                .verifyOnlyRelevantSuggestionWereSuggested();
    }

    @Test
    public void defaultEntityInstancePoolFilter_str_Append_i_Append_STAR() throws IOException {
        page.navigate();
        getInstancePoolSuggestionChecker()
                .filterSuggestions("str")
                .verifyOnlyRelevantSuggestionWereSuggested();

        getInstancePoolSuggestionChecker()
                .appendSymbolToInputField('i')
                .verifyOnlyRelevantSuggestionWereSuggested();

        getInstancePoolSuggestionChecker()
                .appendSymbolToInputField('*')
                .verifyNoneSuggestionWereSuggested();
    }

    @Category(PicketBox.class)
    @Test
    public void defaultSecurityDomainFilter_jboss() throws IOException {
        page.navigate();
        getSecurityDomainSuggestionChecker()
                .filterSuggestions("jboss")
                .verifyOnlyRelevantSuggestionWereSuggested(SECURITY_DOMAIN_FAIL_MESSAGE);
    }

    @Category(PicketBox.class)
    @Test
    public void defaultSecurityDomainFilter_EMPTY() throws IOException {
        page.navigate();
        getSecurityDomainSuggestionChecker()
                .filterSuggestions("jboss")
                .verifyOnlyRelevantSuggestionWereSuggested(SECURITY_DOMAIN_FAIL_MESSAGE);
    }

    @Category(PicketBox.class)
    @Test
    public void defaultSecurityDomainFilter_other() throws IOException {
        page.navigate();
        getSecurityDomainSuggestionChecker()
                .filterSuggestions("other")
                .verifyOnlyRelevantSuggestionWereSuggested(SECURITY_DOMAIN_FAIL_MESSAGE);
    }

    @Category(PicketBox.class)
    @Test
    public void defaultSecurityDomainFilter_aspi() throws IOException {
        page.navigate();
        getSecurityDomainSuggestionChecker()
                .filterSuggestions("aspi")
                .verifyOnlyRelevantSuggestionWereSuggested(SECURITY_DOMAIN_FAIL_MESSAGE);
    }

    @Category(PicketBox.class)
    @Test
    public void defaultSecurityDomainFilterCommonForAllPossibleValues() throws IOException {
        page.navigate();
        getSecurityDomainSuggestionChecker()
                .filterSuggestions("s")
                .verifyOnlyRelevantSuggestionWereSuggested(SECURITY_DOMAIN_FAIL_MESSAGE);
    }

    @Test
    public void defaultSecurityDomainFilter_aspi_Append_i() throws IOException {
        page.navigate();
        getInstancePoolSuggestionChecker()
                .filterSuggestions("aspi")
                .verifyOnlyRelevantSuggestionWereSuggested();

        getInstancePoolSuggestionChecker()
                .appendSymbolToInputField('i')
                .verifyNoneSuggestionWereSuggested();
    }

    @Test
    public void defaultSecurityDomainFilter_aspi_Append_STAR() throws IOException {
        page.navigate();
        getInstancePoolSuggestionChecker()
                .filterSuggestions("aspi")
                .verifyOnlyRelevantSuggestionWereSuggested();

        getInstancePoolSuggestionChecker()
                .appendSymbolToInputField('*')
                .verifyNoneSuggestionWereSuggested();
    }

    @Test
    public void defaultSecurityDomainFilter_STAR() throws IOException {
        page.navigate();
        getInstancePoolSuggestionChecker()
                .filterSuggestions("*")
                .verifyNoneSuggestionWereSuggested();
    }

    @Test
    public void defaultSecurityDomainFilter_foobar() throws IOException {
        page.navigate();
        getInstancePoolSuggestionChecker()
                .filterSuggestions("foobar")
                .verifyNoneSuggestionWereSuggested();
    }

}
