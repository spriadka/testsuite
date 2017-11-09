package org.jboss.hal.testsuite.test.configuration.container;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.page.config.JPAPage;
import org.jboss.hal.testsuite.page.config.StandaloneConfigEntryPoint;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.Console;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class JPATestCase {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);
    private final Operations operations = new Operations(client);

    private static final String DEFAULT_DATASOURCE = "default-datasource";
    private static final String DEFAULT_PERSISTENCE_INHERITANCE = "default-extended-persistence-inheritance";

    private final Address jpaAddress = Address.subsystem("jpa");

    @Drone
    public WebDriver browser;

    @Page
    public JPAPage page;

    @Before
    public void before() {
        FinderNavigation navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class)
                .step(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                .step(FinderNames.SUBSYSTEM, "JPA");

        navigation.selectRow().invoke("View");
        Application.waitUntilVisible();
    }

    @AfterClass
    public static void afterClass() throws IOException, TimeoutException, InterruptedException {
        try {
            administration.restartIfRequired();
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void editDefaultDataSource() throws Exception {
        ModelNodeResult value = operations.readAttribute(jpaAddress, DEFAULT_DATASOURCE);
        try {
            new ConfigChecker.Builder(client, jpaAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, "defaultDataSource", "ExampleDS")
                    .verifyFormSaved()
                    .verifyAttribute(DEFAULT_DATASOURCE, "ExampleDS");
        } finally {
            operations.writeAttribute(jpaAddress, DEFAULT_DATASOURCE, value.value());
        }
    }

    @Test
    public void editPersistenceInheritance() throws Exception {
        ModelNodeResult value = operations.readAttribute(jpaAddress, DEFAULT_PERSISTENCE_INHERITANCE);
        try {
            new ConfigChecker.Builder(client, jpaAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.SELECT, "inheritance", "SHALLOW")
                    .verifyFormSaved()
                    .verifyAttribute(DEFAULT_PERSISTENCE_INHERITANCE, "SHALLOW");

            Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();

            new ConfigChecker.Builder(client, jpaAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.SELECT, "inheritance", "DEEP")
                    .verifyFormSaved()
                    .verifyAttribute(DEFAULT_PERSISTENCE_INHERITANCE, "DEEP");
        } finally {
            operations.writeAttribute(jpaAddress, DEFAULT_PERSISTENCE_INHERITANCE, value.value());
        }
    }

}
