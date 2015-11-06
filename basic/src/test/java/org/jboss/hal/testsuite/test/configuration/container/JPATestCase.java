package org.jboss.hal.testsuite.test.configuration.container;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.CliConstants;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.page.config.JPAPage;
import org.jboss.hal.testsuite.page.config.StandaloneConfigEntryPoint;
import org.jboss.hal.testsuite.test.util.ConfigAreaChecker;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class JPATestCase {

    private CliClient client = CliClientFactory.getClient();
    private ResourceVerifier verifier = new ResourceVerifier(CliConstants.JPA_SUBSYSTEM_ADDRESS, client);
    private ConfigAreaChecker checker = new ConfigAreaChecker(verifier);

    private FinderNavigation navigation;

    @Drone
    public WebDriver browser;

    @Page
    public JPAPage page;

    @Before
    public void before() {
        navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class)
                .addAddress(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                .addAddress(FinderNames.SUBSYSTEM, "JPA");

        navigation.selectRow().invoke("View");
        Application.waitUntilVisible();
    }

    @Test
    public void editDefaultDataSource() {
        checker.editTextAndAssert(page, "defaultDataSource", "ds").dmrAttribute("default-datasource").invoke();
    }

    @Test
    public void editPersistenceInheritance() {
        checker.editSelectAndAssert(page, "inheritance", "SHALLOW").dmrAttribute("default-extended-persistence-inheritance").invoke();
        checker.editSelectAndAssert(page, "inheritance", "DEEP").dmrAttribute("default-extended-persistence-inheritance").invoke();
    }

}
