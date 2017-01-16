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
import org.junit.Assert;
import org.openqa.selenium.WebDriver;

/**
 * @author jcechace
 */
public abstract class AbstractTestConnectionTestCase {

    /**
     * Hook which method will be executed after test connection was performed in wizard
     */
    protected interface AfterConnectionTestHook {
        void afterConnectionWasTested(DatasourceWizard wizard);
    }

    private static final String
            NAME = "name",
            JNDI_NAME = "jndiName",
            CONNECTION_URL = "connectionUrl",
            URL = "URL",
            DETECTED_DRIVER = "h2";

    @Drone
    protected WebDriver browser;

    @Page
    protected DatasourcesPage datasourcesPage;

    /**
     * Tests connection in datasource view
     * @param expected true if connection is expected to succeed, false otherwise
     */
    protected void testConnectionInDatasourceView(boolean expected) {
        DatasourceConfigArea config = datasourcesPage.getConfig();
        ConnectionConfig connection = config.connectionConfig();
        TestConnectionWindow window = connection.testConnection();

        assertConnectionTest(window, expected);
    }

    /**
     * Tests connection in already opened wizard
     * @param name name of datasource
     * @param url connection url
     * @param expected true if connection is expected to succeed, false otherwise
     * @param hook hook which will be executed after test connection is performed
     */
    protected void testConnectionInWizard(String name, String url, boolean expected, AfterConnectionTestHook hook) {
        DatasourceWizard wizard = datasourcesPage.getDatasourceWizard();
        DatasourceWizard.DatasourceType type = DatasourceWizard.DatasourceType.NON_XA;
        Editor editor = wizard.getEditor();

        wizard.goToLocation(DatasourceWizard.Location.DATASOURCE_ATTRIBUTES, type);

        editor.text(NAME, name);
        editor.text(JNDI_NAME, "java:/" + name);

        wizard.goToLocation(DatasourceWizard.Location.JDBC_DRIVER, type);

        wizard.switchToDetectedDriver();
        wizard.selectDriver(DETECTED_DRIVER);

        wizard.goToLocation(DatasourceWizard.Location.CONNECTION_SETTINGS, type);

        editor.text(CONNECTION_URL, url);

        wizard.goToLocation(DatasourceWizard.Location.TEST_CONNECTION, type);

        assertConnectionTest(wizard.testConnection(), expected);

        hook.afterConnectionWasTested(wizard);
    }

    /**
     * Tests connection in already opened wizard and cancels the wizard afterwards.
     * @param name name of datasource
     * @param url connection url
     * @param expected true if connection is expected to succeed, false otherwise
     * @see AbstractTestConnectionTestCase#testConnectionInWizard(String, String, boolean, AfterConnectionTestHook)
     */
    protected void testConnectionInWizardAndCancel(String name, String url, boolean expected) {
        testConnectionInWizard(name, url, expected, (DatasourceWizard wizard) ->
                datasourcesPage.getDatasourceWizard().cancelAndDismissReloadRequiredWindow());
    }

    /**
     * Tests connection in already opened wizard and clicks on cross in top right corner afterwards.
     * @param name name of datasource
     * @param url connection url
     * @param expected true if connection is expected to succeed, false otherwise
     * @see AbstractTestConnectionTestCase#testConnectionInWizard(String, String, boolean, AfterConnectionTestHook)
     */
    protected void testConnectionInWizardAndClose(String name, String url, boolean expected) {
        testConnectionInWizard(name, url, expected, (DatasourceWizard wizard) ->
                datasourcesPage.getDatasourceWizard().close());
    }

    /**
     * Tests connection in already opened wizard and saves it afterwards.
     * @param name name of datasource
     * @param url connection url
     * @param expected true if connection is expected to succeed, false otherwise
     * @see AbstractTestConnectionTestCase#testConnectionInWizard(String, String, boolean, AfterConnectionTestHook)
     */
    protected void testConnectionInWizardAndSave(String name, String url, boolean expected) {
        testConnectionInWizard(name, url, expected, (DatasourceWizard wizard) -> {
            wizard.goToLocation(DatasourceWizard.Location.SUMMARY, DatasourceWizard.DatasourceType.NON_XA);
            wizard.finishAndDismissReloadRequiredWindow();
        });
    }

    /**
     * Tests connection for XA datasource in already opened wizard
     * @param name name of datasource
     * @param url connection url
     * @param expected true if connection is expected to succeed, false otherwise
     * @param hook hook which will be executed after test connection is performed
     */
    protected void testXAConnectionInWizard(String name, String url, boolean expected, AfterConnectionTestHook hook) {
        DatasourceWizard wizard = datasourcesPage.getDatasourceWizard();
        DatasourceWizard.DatasourceType type = DatasourceWizard.DatasourceType.XA;
        Editor editor = wizard.getEditor();

        wizard.goToLocation(DatasourceWizard.Location.DATASOURCE_ATTRIBUTES, type);

        editor.text(NAME, name);
        editor.text(JNDI_NAME, "java:/" + name);

        wizard.goToLocation(DatasourceWizard.Location.JDBC_DRIVER, type);

        wizard.switchToDetectedDriver();
        wizard.selectDriver(DETECTED_DRIVER);

        wizard.goToLocation(DatasourceWizard.Location.XA_PROPERTIES, type);

        PropertyEditor properties = editor.properties();
        properties.add(URL, url);

        wizard.goToLocation(DatasourceWizard.Location.TEST_CONNECTION, type);

        assertConnectionTest(wizard.testConnection(), expected);

        hook.afterConnectionWasTested(wizard);
    }

    /**
     * Tests connection for XA datasource in already opened wizard and cancels the wizard afterwards.
     * @param name name of datasource
     * @param url connection url
     * @param expected true if connection is expected to succeed, false otherwise
     * @see AbstractTestConnectionTestCase#testXAConnectionInWizard(String, String, boolean, AfterConnectionTestHook)
     */
    protected void testXAConnectionInWizardAndCancel(String name, String url, boolean expected) {
        testXAConnectionInWizard(name, url, expected, (DatasourceWizard wizard) ->
                datasourcesPage.getDatasourceWizard().cancelAndDismissReloadRequiredWindow());
    }

    /**
     * Tests connection for XA datasource in already opened wizard and clicks on cross in top right corner afterwards.
     * @param name name of datasource
     * @param url connection url
     * @param expected true if connection is expected to succeed, false otherwise
     * @see AbstractTestConnectionTestCase#testXAConnectionInWizard(String, String, boolean, AfterConnectionTestHook)
     */
    protected void testXAConnectionInWizardAndClose(String name, String url, boolean expected) {
        testXAConnectionInWizard(name, url, expected, (DatasourceWizard wizard) ->
                datasourcesPage.getDatasourceWizard().close());
    }

    /**
     * Tests connection for XA datasource in already opened wizard and saves it afterwards.
     * @param name name of datasource
     * @param url connection url
     * @param expected true if connection is expected to succeed, false otherwise
     * @see AbstractTestConnectionTestCase#testXAConnectionInWizard(String, String, boolean, AfterConnectionTestHook)
     */
    protected void testXAConnectionInWizardAndSave(String name, String url, boolean expected) {
        testXAConnectionInWizard(name, url, expected, (DatasourceWizard wizard) -> {
            wizard.goToLocation(DatasourceWizard.Location.SUMMARY, DatasourceWizard.DatasourceType.XA);
            wizard.finishAndDismissReloadRequiredWindow();
        });
    }

    /**
     * Assert expected value to test connection result
     * @param window window with connection result
     * @param expected true if connection is expected to succeed, false otherwise
     */
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
