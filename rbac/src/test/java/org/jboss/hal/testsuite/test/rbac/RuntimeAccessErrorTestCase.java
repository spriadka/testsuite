package org.jboss.hal.testsuite.test.rbac;

import junit.framework.Assert;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.runtime.StandaloneRuntimeEntryPoint;
import org.jboss.hal.testsuite.util.Authentication;
import org.jboss.hal.testsuite.util.RbacRole;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 11.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class RuntimeAccessErrorTestCase {

    private FinderNavigation navigation;

    @Drone
    private WebDriver browser;

    @Test
    public void administrator() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.ADMINISTRATOR);
        navigateToRuntimeSections();
    }

    @Test
    public void monitor() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.MONITOR);
        navigateToRuntimeSections();
    }

    @Test
    public void operator() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.OPERATOR);
        navigateToRuntimeSections();
    }

    public void navigateToRuntimeSections() throws Exception {
        List<String> acc = new ArrayList<String>();
        String path = "/StandaloneServer";
        String pathSubsystem = path + "/Subsystem";
        navigation = new FinderNavigation(browser, StandaloneRuntimeEntryPoint.class)
                .addAddress(FinderNames.SERVER, FinderNames.STANDALONE_SERVER)
                .addAddress(FinderNames.MONITOR, "JVM");
        navigateToPageAndStorePathOnError(navigation, acc, path + "/JVM");

        navigation = new FinderNavigation(browser, StandaloneRuntimeEntryPoint.class)
                .addAddress(FinderNames.SERVER, FinderNames.STANDALONE_SERVER)
                .addAddress(FinderNames.MONITOR, "Environment");
        navigateToPageAndStorePathOnError(navigation, acc, path + "/Environment");

        navigation = new FinderNavigation(browser, StandaloneRuntimeEntryPoint.class)
                .addAddress(FinderNames.SERVER, FinderNames.STANDALONE_SERVER)
                .addAddress(FinderNames.MONITOR, FinderNames.LOG_FILES);
        navigateToPageAndStorePathOnError(navigation, acc, path + "/LogFiles");

        navigation = new FinderNavigation(browser, StandaloneRuntimeEntryPoint.class)
                .addAddress(FinderNames.SERVER, FinderNames.STANDALONE_SERVER)
                .addAddress(FinderNames.MONITOR, FinderNames.SUBSYSTEMS)
                .addAddress(FinderNames.SUBSYSTEM, "Datasources");
        navigateToPageAndStorePathOnError(navigation, acc, pathSubsystem + "/Datasources");

        navigation = new FinderNavigation(browser, StandaloneRuntimeEntryPoint.class)
                .addAddress(FinderNames.SERVER, FinderNames.STANDALONE_SERVER)
                .addAddress(FinderNames.MONITOR, FinderNames.SUBSYSTEMS)
                .addAddress(FinderNames.SUBSYSTEM, "JPA");
        navigateToPageAndStorePathOnError(navigation, acc, pathSubsystem + "/JPA");

        navigation = new FinderNavigation(browser, StandaloneRuntimeEntryPoint.class)
                .addAddress(FinderNames.SERVER, FinderNames.STANDALONE_SERVER)
                .addAddress(FinderNames.MONITOR, FinderNames.SUBSYSTEMS)
                .addAddress(FinderNames.SUBSYSTEM, "JNDI View");
        navigateToPageAndStorePathOnError(navigation, acc, pathSubsystem + "/JNDI_View");

        navigation = new FinderNavigation(browser, StandaloneRuntimeEntryPoint.class)
                .addAddress(FinderNames.SERVER, FinderNames.STANDALONE_SERVER)
                .addAddress(FinderNames.MONITOR, FinderNames.SUBSYSTEMS)
                .addAddress(FinderNames.SUBSYSTEM, "Transactions");
        navigateToPageAndStorePathOnError(navigation, acc, pathSubsystem + "/Transacitons");

        navigation = new FinderNavigation(browser, StandaloneRuntimeEntryPoint.class)
                .addAddress(FinderNames.SERVER, FinderNames.STANDALONE_SERVER)
                .addAddress(FinderNames.MONITOR, FinderNames.SUBSYSTEMS)
                .addAddress(FinderNames.SUBSYSTEM, "Transaction Logs");
        navigateToPageAndStorePathOnError(navigation, acc, pathSubsystem + "/TransactionLogs");

        navigation = new FinderNavigation(browser, StandaloneRuntimeEntryPoint.class)
                .addAddress(FinderNames.SERVER, FinderNames.STANDALONE_SERVER)
                .addAddress(FinderNames.MONITOR, FinderNames.SUBSYSTEMS)
                .addAddress(FinderNames.SUBSYSTEM, "HTTP"); //domain mode different
        navigateToPageAndStorePathOnError(navigation, acc,pathSubsystem + "/HTTP");

        navigation = new FinderNavigation(browser, StandaloneRuntimeEntryPoint.class)
                .addAddress(FinderNames.SERVER, FinderNames.STANDALONE_SERVER)
                .addAddress(FinderNames.MONITOR, FinderNames.SUBSYSTEMS)
                .addAddress(FinderNames.SUBSYSTEM, "Webservices");
        navigateToPageAndStorePathOnError(navigation, acc, pathSubsystem + "/WebServices");

        assertTrue("Insufficient privileges on following paths " + getPathsWithError(acc), acc.isEmpty());
    }

    public void navigateToPageAndStorePathOnError(FinderNavigation navigation, List<String> acc, String path)
            throws Exception {
        //messages
        navigation.selectRow().invoke("View");
        Application.waitUntilVisible();
        containsInsufficientPrivilegesWindow();
        if (containsInsufficientPrivilegesWindow()) {
            acc.add(path + " \n");
        }
    }

    public String getPathsWithError(List<String> paths) {
        StringBuilder builder = new StringBuilder();
        for (String p : paths) {
            builder.append("\n");
            builder.append(p);
        }
        return builder.toString();
    }

    public boolean containsInsufficientPrivilegesWindow() {
        WebElement error = null;
        try {
            error =  browser.findElement(By.className("default-window"));
        } catch (NoSuchElementException ex) { }
        finally {
            return (error != null);
        }
    }
}
