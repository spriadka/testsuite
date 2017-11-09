package org.jboss.hal.testsuite.test.configuration.jgroups;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.creaper.command.BackupAndRestoreAttributes;
import org.jboss.hal.testsuite.util.Console;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;

import java.io.IOException;

@RunWith(Arquillian.class)
public class JGroupUDPTestCase extends JGroupAbstractTestCase {

    private static BackupAndRestoreAttributes backup;

    @BeforeClass
    public static void setUpUDP() throws IOException, CommandFailedException {
        BASE_ADDRESS =  JGROUPS_ADDRESS.and("stack", "udp");
        PROTOCOL_ADDRESS = BASE_ADDRESS.and("protocol", DEFAULT_PROTOCOL);
        TRANSPORT_ADDRESS = BASE_ADDRESS.and("transport", "UDP");
        backup = new BackupAndRestoreAttributes.Builder(TRANSPORT_ADDRESS).build();
        client.apply(backup.backup());
        jGroupsOperations.addProperty(TRANSPORT_ADDRESS, TRANSPORT_PROPERTY_TBR, RandomStringUtils.randomAlphanumeric(5));
        jGroupsOperations.addProperty(PROTOCOL_ADDRESS, PROTOCOL_PROPERTY_TBR, PROTOCOL_PROPERTY_TBR_VALUE);
    }

    @AfterClass
    public static void afterClass() throws CommandFailedException {
        client.apply(backup.restore());
    }

    @Before
    public void beforeUDP() {
        page.selectStackByName("udp");
        Console.withBrowser(browser).waitUntilLoaded();
        page.switchToTransport();
    }
}
