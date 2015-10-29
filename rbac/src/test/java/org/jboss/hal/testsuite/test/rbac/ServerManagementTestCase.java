package org.jboss.hal.testsuite.test.rbac;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.ResourceVerifier;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.page.runtime.DomainRuntimeEntryPoint;
import org.jboss.hal.testsuite.util.Authentication;
import org.jboss.hal.testsuite.util.RbacRole;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertEquals;

/**
 * Created by pcyprian on 16.10.15.
 */
@RunWith(Arquillian.class)
@Category(Domain.class)
public class ServerManagementTestCase {

    private FinderNavigation navigation;

    private ResourceAddress address;
    static Dispatcher dispatcher;
    static ResourceVerifier verifier;

    @BeforeClass
    public static void beforeClass() {
        dispatcher = new Dispatcher();
        verifier  = new ResourceVerifier(dispatcher);
    }

    @AfterClass
    public static void afterClass() {
        dispatcher.close();
    }

    @Drone
    private WebDriver browser;


    @Test
    public void monitor() {
        Authentication.with(browser).authenticate(RbacRole.MONITOR);

        startServer(false, "master", "server-one");
        startServer(false, "slave", "server-one");

        stopServer(false, "master", "server-one");
        stopServer(false, "slave", "server-one");
    }

    @Test // https://issues.jboss.org/browse/HAL-909
    public void operator() {
        Authentication.with(browser).authenticate(RbacRole.OPERATOR);

        startServer(true, "master", "server-one");
        startServer(true, "master", "server-three");
        startServer(true, "slave", "server-one");

        stopServer(true, "master", "server-one");
        stopServer(true, "master", "server-three");
        stopServer(true, "slave", "server-one");
    }

    @Test // https://issues.jboss.org/browse/HAL-909
    public void mainOperator() {
        Authentication.with(browser).authenticate(RbacRole.MAIN_OPERATOR);

        startServer(true, "master", "server-one");
        startServer(false, "master", "server-three");
        startServer(true, "slave", "server-one");

        stopServer(true, "master", "server-one");
        stopServer(false, "master", "server-three");
        stopServer(true, "slave", "server-one");
    }

    @Test
    public void hostMasterOperator() {
        Authentication.with(browser).authenticate(RbacRole.HOST_MASTER_OPERATOR);

        startServer(false, "master", "server-one");
        startServer(false, "master", "server-three");
        //startServer(true, "slave", "server-one"); should be slave there or no?

        stopServer(false, "master", "server-one");
        stopServer(false, "master", "server-three");
        //stopServer(true, "slave", "server-one");
    }

    public void stopServer(boolean shouldSucceed, String host, String server) {
        boolean missingBtn = false;
        navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                .addAddress(FinderNames.BROWSE_DOMAIN_BY, FinderNames.HOSTS)
                .addAddress(FinderNames.HOST, host)
                .addAddress("Server", server);

        try {
            navigation.selectRow().invoke("Start");
        } catch (NoSuchElementException ex) {
            missingBtn = true;
        }

        assertEquals("Missing stop btn for server - " + server + " in host - " + host, missingBtn, !shouldSucceed);

        if (shouldSucceed) {
            address = new ResourceAddress(new ModelNode("/host=" + host + "/server=" + server));

            verifier.verifyAttribute(address, "server-state", "STOPPED");
        }
    }

    public void startServer(boolean shouldSucceed, String host, String server) {
        boolean missingBtn = false;
        navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                .addAddress(FinderNames.BROWSE_DOMAIN_BY, FinderNames.HOSTS)
                .addAddress(FinderNames.HOST, host)
                .addAddress("Server", server);

        try {
           navigation.selectRow().invoke("Start");
        } catch (NoSuchElementException ex) {
            missingBtn = true;
        }

         assertEquals("Missing start btn for server - " + server + " in host - " + host, missingBtn, !shouldSucceed);

        if (shouldSucceed) {
            address = new ResourceAddress(new ModelNode("/host=" + host + "/server=" + server));

            verifier.verifyAttribute(address, "server-state", "running");
        }

    }

}
