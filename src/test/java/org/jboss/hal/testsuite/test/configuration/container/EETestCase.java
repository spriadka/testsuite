package org.jboss.hal.testsuite.test.configuration.container;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.CliConstants;
import org.jboss.hal.testsuite.page.config.EEPage;
import org.jboss.hal.testsuite.test.category.Standalone;
import org.jboss.hal.testsuite.test.util.ConfigAreaChecker;
import org.jboss.hal.testsuite.util.Console;
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
public class EETestCase {

    private CliClient client = CliClientFactory.getClient();
    private ResourceVerifier verifier = new ResourceVerifier(CliConstants.EE_SUBSYSTEM_ADDRESS, client);
    private ConfigAreaChecker checker = new ConfigAreaChecker(verifier);

    @Drone
    public WebDriver browser;

    @Page
    public EEPage page;

    @Before
    public void before(){
        Console.withBrowser(browser).refreshAndNavigate(EEPage.class);
    }

    @Test
    public void toggleIsolatedDeployments(){
        checker.editCheckboxAndAssert(page, "ear-subdeployments-isolated", true).invoke();
        checker.editCheckboxAndAssert(page, "ear-subdeployments-isolated", false).invoke();
    }
}
