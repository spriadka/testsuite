package org.jboss.hal.testsuite.test.configuration.tools;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.page.config.ELResolverPage;
import org.jboss.hal.testsuite.util.Console;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import static org.junit.Assert.assertEquals;


/**
 * Created by pcyprian on 23.10.15.
 */
@RunWith(Arquillian.class)
@Category(Domain.class)
public class ELResolverTestCase {
    private static final String NAME = "pr_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String VALUE = "val_" + RandomStringUtils.randomAlphanumeric(5);

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);

    @Drone
    private WebDriver browser;

    @Page
    private ELResolverPage page;

    @Before
    public void before() {
        page.navigate();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
    }

    @AfterClass
    public static void tearDown() {
        IOUtils.closeQuietly(client);
    }

    @Test
    public void resolveSystemPropertyValue() throws Exception {
        operations.add(Address.root().and("system-property", NAME), Values.of("value", VALUE));

        try {
            page.openExpressionResolver();
            String output = page.resolveSystemProperty(NAME);
            String expected = "server-one=" + VALUE + "\nserver-two=" + VALUE + "\n";
            assertEquals(expected, output);
        } finally {
            operations.remove(Address.root().and("system-property", NAME));
        }
    }

    @Test
    public void resolveServerPropertyWithSameValue() throws Exception {
        addServerProperty(NAME, VALUE, "server-one");
        addServerProperty(NAME, VALUE, "server-two");
        try {
            page.openExpressionResolver();
            String output = page.resolveSystemProperty(NAME);
            String expected = "server-one=" + VALUE + "\nserver-two=" + VALUE + "\n";
            assertEquals(expected, output);
        } finally {
            removeServerProperty(NAME, "server-one");
            removeServerProperty(NAME, "server-two");
        }
    }

    @Test
    public void resolveServerPropertyWithDifferentValue() throws Exception {
        String reverseValue = new StringBuilder(VALUE).reverse().toString();

        addServerProperty(NAME, VALUE, "server-one");
        addServerProperty(NAME, reverseValue, "server-two");
        try {
            page.openExpressionResolver();
            String output = page.resolveSystemProperty(NAME);
            String expected = "server-one=" + VALUE + "\nserver-two=" + reverseValue + "\n";
            assertEquals(expected, output);
        } finally {
            removeServerProperty(NAME, "server-one");
            removeServerProperty(NAME, "server-two");
        }
    }

    @Test
    public void resolveServerPropertyDefinedOnOneServer() throws Exception {
        addServerProperty(NAME, VALUE, "server-one");
        try {
            page.openExpressionResolver();
            String output = page.resolveSystemProperty(NAME);
            String expected = "server-one=" + VALUE + "\nserver-two=default_value\n";
            assertEquals(expected, output);

        } finally {
            removeServerProperty(NAME, "server-one");
        }
    }

    private void addServerProperty(String name, String value, String server) throws Exception {
        Address address = getServerPropertyAddress(name, server);
        operations.add(address, Values.of("value", value));
        new ResourceVerifier(address, client, 500).verifyExists();
    }

    private void removeServerProperty(String name, String server) throws Exception {
        Address address = getServerPropertyAddress(name, server);
        operations.remove(address);
        new ResourceVerifier(address, client, 500).verifyDoesNotExist();
    }

    private Address getServerPropertyAddress(String name, String server) {
        return Address.host(client.options().defaultHost)
                .and("server-config", server)
                .and("system-property", name);
    }
}
