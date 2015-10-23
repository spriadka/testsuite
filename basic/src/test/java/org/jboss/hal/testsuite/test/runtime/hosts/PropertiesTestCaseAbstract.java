package org.jboss.hal.testsuite.test.runtime.hosts;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.fragment.runtime.HostPropertiesWizard;
import org.jboss.hal.testsuite.page.runtime.HostsPage;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 23.10.15.
 */
public abstract class PropertiesTestCaseAbstract {

    @Drone
    protected WebDriver browser;

    @Page
    protected HostsPage page;

    private static Dispatcher dispatcher;

    @BeforeClass
    public static void setUp() {
        dispatcher = new Dispatcher();
    }

    @Before
    public void mainBefore() {
        navigate();
    }

    @InSequence(0)
    @Test
    public void createServerProperty() {
        HostPropertiesWizard wizard = page.addProperty();
        wizard.name("test")
                .value("example")
                .bootTime(true);
        wizard.finish();
        Assert.assertTrue("Property should be present in table", page.isRowPresent("test"));
    }

    @InSequence(1)
    @Test
    public void removeServerProperty() {
        page.getResourceManager().removeResource("test").confirm();
        Assert.assertFalse("Property should not be present in table", page.isRowPresent("test"));
    }

    @Test
    public void invalidPropertyName() {
        HostPropertiesWizard wizard = page.addProperty();
        wizard.name("test *+<")
                .value("example")
                .bootTime(true);
        wizard.finish();
        Assert.assertTrue("Error should be shown", page.isErrorShown());
        wizard.cancel();
    }

    @Test
    public void invalidPropertyValue() {
        HostPropertiesWizard wizard = page.addProperty();
        wizard.name("test")
                .value("example *+<")
                .bootTime(true);
        wizard.finish();
        Assert.assertTrue("Error should be shown", page.isErrorShown());
        wizard.cancel();
    }

    @Test
    public void createServerPropertyWithFalseBootTime() {
        HostPropertiesWizard wizard = page.addProperty();
        wizard.name("falseBootTime")
                .value("example")
                .bootTime(false);
        wizard.finish();
        Assert.assertTrue("Property should be present in table", page.isRowPresent("test"));
    }

    @AfterClass
    public static void tearDown() {
        dispatcher.close();
    }

    protected abstract void navigate();
}
