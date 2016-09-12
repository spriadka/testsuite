package org.jboss.hal.testsuite.test.runtime.server.configurationchanges;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.BackupAndRestoreAttributes;
import org.jboss.hal.testsuite.page.runtime.ConfigurationChangesPage;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.configurationchanges.ConfigurationChange;
import org.jboss.hal.testsuite.util.configurationchanges.ConfigurationChangesResource;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 9/14/16.
 */

@RunWith(Arquillian.class)
@Category(Shared.class)
public class ConfigurationChangesTestCase {

    //TODO remove https://issues.jboss.org/browse/HAL-1182 message when fixed
    private static final String LISTS_NOT_EQUAL_MESSAGE = "Some unexpected differences found! Either some changes are " +
            "not displayed or some extra appeared. Some could fail due to the https://issues.jboss.org/browse/HAL-1182 " +
            "because of composite operation having its name replaced by a operation step's operation name.";

    @Drone
    private WebDriver browser;

    @Page
    private ConfigurationChangesPage page;

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);
    private static final Administration administration = new Administration(client);
    private static Address serviceAddress;
    private static ConfigurationChangesResource changesResource;

    @BeforeClass
    public static void beforeClass() {
        serviceAddress = client.options().isDomain ?
                Address.host(client.options().defaultHost)
                        .and("core-service", "management")
                        .and("service", "configuration-changes") :
                Address.coreService("management")
                        .and("service", "configuration-changes");
        changesResource = new ConfigurationChangesResource(client);
    }

    @Before
    public void before() throws IOException, OperationException, TimeoutException, InterruptedException {
        final int MAX_HISTORY = 100;
        //make sure the service is up and running
        if (!operations.exists(serviceAddress)) {
            operations.add(serviceAddress, Values.of("max-history", MAX_HISTORY)).assertSuccess();
        } else {
            operations.writeAttribute(serviceAddress, "max-history", MAX_HISTORY).assertSuccess();
        }
        administration.reloadIfRequired();
        page.navigate();
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException, TimeoutException, InterruptedException {
        try {
            operations.removeIfExists(serviceAddress);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void disableAndEnableTracking() throws Exception {
        final int MAX_HISTORY = 42;

        page.disable();
        new ResourceVerifier(serviceAddress, client)
                .verifyDoesNotExist();

        page.openEnableConfigurationChangesDialog()
                .maxHistory(MAX_HISTORY)
                .clickSave();
        new ResourceVerifier(serviceAddress, client)
                .verifyAttribute("max-history", MAX_HISTORY);
    }

    @Test
    public void allRelevantRecordsAreShown() throws IOException, ParseException {
        Assert.assertTrue(LISTS_NOT_EQUAL_MESSAGE,
                isChangesListsEqual(changesResource.getAllConfigurationChanges(),
                                    page.getConfigurationChangesTable().getAllConfigurationChanges()));
    }

    @Test
    public void filterConfigurationChangesCapabilities() throws Exception {
        List<ConfigurationChange> filtered_S_configurationChanges = page.filter().setFilter("s").getAllConfigurationChanges();
        Assert.assertTrue(LISTS_NOT_EQUAL_MESSAGE,
                isChangesListsEqual(changesResource.getFilteredConfigurationChanges(page.filter().getCurrentFilter()),
                                    filtered_S_configurationChanges));

        List<ConfigurationChange> filtered_SU_configurationChanges = page.filter().appendSymbol('u').getAllConfigurationChanges();
        Assert.assertTrue(LISTS_NOT_EQUAL_MESSAGE,
                isChangesListsEqual(changesResource.getFilteredConfigurationChanges(page.filter().getCurrentFilter()),
                                    filtered_SU_configurationChanges));

        List<ConfigurationChange> filtered_STAR_configurationChanges = page.filter().appendSymbol('*').getAllConfigurationChanges();
        Assert.assertTrue("Some unexpected configuration changes appeared.", filtered_STAR_configurationChanges.isEmpty());

        Assert.assertTrue(LISTS_NOT_EQUAL_MESSAGE,
                isChangesListsEqual(filtered_SU_configurationChanges,
                                    page.filter().removeSymbol().getAllConfigurationChanges()));
    }

    @Test
    public void generateSomeRecordsAndVerifyAllRelevantRecordsAreShownAfterRefresh() throws CommandFailedException, IOException, ParseException, TimeoutException, InterruptedException {
        BackupAndRestoreAttributes backup = new BackupAndRestoreAttributes.Builder(Address.subsystem("datasources").and("data-source", "ExampleDS"))
                .build();
        client.apply(backup.backup());
        client.apply(backup.restore());
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        administration.reloadIfRequired();

        Assert.assertFalse("They definitely should not be equal, this is before refresh state",
                isChangesListsEqual(changesResource.getAllConfigurationChanges(),
                                    page.getConfigurationChangesTable().getAllConfigurationChanges()));

        page.refresh();

        Assert.assertTrue(LISTS_NOT_EQUAL_MESSAGE,
                isChangesListsEqual(changesResource.getAllConfigurationChanges(),
                                    page.getConfigurationChangesTable().getAllConfigurationChanges()));
    }

    private boolean areChangesEqual(ConfigurationChange a, ConfigurationChange b) {
        if ((a == null && b != null) || (a != null && b == null)) {
            return false;
        }

        if (a == null && b == null) {
            return true;
        }

        try {
            return a.getDatetime().equals(b.getDatetime()) &&
                    a.getAccessMechanism().equals(b.getAccessMechanism()) &&
                    a.getRemoteAddress().equals(b.getRemoteAddress()) &&
                    a.getResourceAddress().equals(b.getResourceAddress()) &&
                    a.getOperation().equals(b.getOperation()) &&
                    a.getResult().equals(b.getResult());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isChangesListsEqual(List<ConfigurationChange> l1, List<ConfigurationChange> l2) {
        if (l1.size() != l2.size()) {
            return false;
        }
        for (ConfigurationChange change : l1) {
            boolean equals = false;
            for (ConfigurationChange comparedChange : l2) {
                if (areChangesEqual(change, comparedChange)) {
                    equals = true;
                }
            }
            if (!equals) {
                return false;
            }
        }
        return true;
    }
}
