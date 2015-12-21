package org.jboss.hal.testsuite.test.configuration.datasources;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.fragment.config.datasource.ConnectionConfig;
import org.jboss.hal.testsuite.fragment.config.datasource.DatasourceConfigArea;
import org.jboss.hal.testsuite.fragment.config.datasource.DatasourceWizard;
import org.jboss.hal.testsuite.fragment.config.datasource.TestConnectionWindow;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.formeditor.PropertyEditor;
import org.jboss.hal.testsuite.page.config.DatasourcesPage;
import org.jboss.hal.testsuite.util.Console;
import org.junit.Assert;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import java.io.IOException;

/**
 * @author jcechace
 */
public abstract class AbstractTestConnectionTestCase {

    @Drone
    protected WebDriver browser;

    @Page
    protected DatasourcesPage datasourcesPage;

    protected void testConnection(String name, boolean expected) {
        datasourcesPage.view(name);
        Console.withBrowser(browser).waitUntilLoaded();

        DatasourceConfigArea config = datasourcesPage.getConfig();
        ConnectionConfig connection = config.connectionConfig();
        TestConnectionWindow window = connection.testConnection();

        assertConnectionTest(window, expected);
    }

    protected void testConnectionInWizard(DataSourcesOperations dsOps, String name, String url, boolean expected) throws IOException, OperationException {
        DatasourceWizard wizard = datasourcesPage.addResource();
        Editor editor = wizard.getEditor();

        wizard.next();

        editor.text("name", name);
        editor.text("jndiName", "java:/" + name);
        wizard.next();

        wizard.switchToDetectedDriver();
        wizard.next();

        editor.text("connectionUrl", url);

        assertConnectionTest(wizard.testConnection(), expected);
        Address dsAddress = DataSourcesOperations.getDsAddress(name);
        Assert.assertFalse(dsAddress + " shouldn't exist", dsOps.exists(dsAddress));
    }

    protected void testXAConnectionInWizard(DataSourcesOperations dsOps, String name, String url, boolean expected) throws IOException, OperationException {
        DatasourceWizard wizard = datasourcesPage.addResource();
        Editor editor = wizard.getEditor();

        wizard.next();

        editor.text("name", name);
        editor.text("jndiName", "java:/" + name);
        wizard.next();

        wizard.switchToDetectedDriver();
        wizard.next();

        PropertyEditor properties = editor.properties();
        properties.add("URL", url);

        wizard.next();

        assertConnectionTest(wizard.testConnection(), expected);
        Address dsAddress = DataSourcesOperations.getXADsAddress(name);
        Assert.assertFalse(dsAddress + " shouldn't exist", dsOps.exists(dsAddress));
    }

    protected void assertConnectionTest(TestConnectionWindow window, boolean expected) {
        boolean result = window.isSuccessful();
        window.close();

        if (expected) {
            Assert.assertTrue("Connection test was expected to succeed", result);
        } else {
            Assert.assertFalse("Connection test was expected to fail", result);
        }
    }
}
