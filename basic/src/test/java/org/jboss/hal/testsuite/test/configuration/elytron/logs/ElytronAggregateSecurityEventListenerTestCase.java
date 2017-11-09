package org.jboss.hal.testsuite.test.configuration.elytron.logs;

import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.AddResourceWizard;
import org.jboss.hal.testsuite.page.config.elytron.ElytronOtherOtherPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.Arrays;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(Elytron.class)
@RunWith(Arquillian.class)
public class ElytronAggregateSecurityEventListenerTestCase extends AbstractElytronTestCase {

    private static final String
            AGGREGATE_SECURITY_EVENT_LISTENER = "aggregate-security-event-listener",
            AGGREGATE_SECURITY_EVENT_LISTENER_LABEL = "Aggregate Security Event Listener",
            FILE_AUDIT_LOG = "file-audit-log",
            PATH = "path",
            SECURITY_EVENT_LISTENERS = "security-event-listeners";

    @Page
    private ElytronOtherOtherPage page;

    /**
     * @tpTestDetails Try to create Aggregate Security Event Listener instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Aggregate Security Event Listener table.
     * Validate created resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void testAddAggregateSecurityEventListener() throws Exception {
        final Address eventListener1 = elyOps.getElytronAddress(FILE_AUDIT_LOG, randomAlphabetic(7)),
                eventListener2 = elyOps.getElytronAddress(FILE_AUDIT_LOG, randomAlphabetic(7)),
                aggregateSecurityEventListenerAddress = elyOps.getElytronAddress(AGGREGATE_SECURITY_EVENT_LISTENER,
                        randomAlphabetic(7));
        try {
            createFileAuditLog(eventListener1);
            createFileAuditLog(eventListener2);

            page.navigateToApplication()
                    .switchSubTab(AGGREGATE_SECURITY_EVENT_LISTENER_LABEL);

            page.getResourceManager()
                    .addResource(AddResourceWizard.class)
                    .name(aggregateSecurityEventListenerAddress.getLastPairValue())
                    .text(SECURITY_EVENT_LISTENERS, String.join("\n",
                            new String[]{eventListener1.getLastPairValue(), eventListener2.getLastPairValue()}))
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

            assertTrue(page.getResourceManager()
                    .isResourcePresent(aggregateSecurityEventListenerAddress.getLastPairValue()));

            new ResourceVerifier(aggregateSecurityEventListenerAddress, client).verifyExists()
                    .verifyAttribute(SECURITY_EVENT_LISTENERS, new ModelNodeGenerator.ModelNodeListBuilder()
                            .addNode(new ModelNode(eventListener1.getLastPairValue()))
                            .addNode(new ModelNode(eventListener2.getLastPairValue()))
                            .build());
        } finally {
            ops.removeIfExists(aggregateSecurityEventListenerAddress);
            ops.removeIfExists(eventListener1);
            ops.removeIfExists(eventListener2);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Aggregate Security Event Listener instance in model and try to remove it in Web Console's
     * Elytron subsystem configuration.
     * Validate the resource is not any more visible in Aggregate Security Event Listener table.
     * Validate removed resource is not any more present in the model.
     */
    @Test
    public void testRemoveAggregateSecurityEventListener() throws Exception {
        final Address eventListener1 = elyOps.getElytronAddress(FILE_AUDIT_LOG, randomAlphabetic(7)),
                eventListener2 = elyOps.getElytronAddress(FILE_AUDIT_LOG, randomAlphabetic(7)),
                aggregateSecurityEventListenerAddress = elyOps.getElytronAddress(AGGREGATE_SECURITY_EVENT_LISTENER,
                        randomAlphabetic(7));
        try {
            createFileAuditLog(eventListener1);
            createFileAuditLog(eventListener2);
            createAggregateSecurityEventListener(aggregateSecurityEventListenerAddress, eventListener1, eventListener2);

            page.navigateToApplication()
                    .switchSubTab(AGGREGATE_SECURITY_EVENT_LISTENER_LABEL);

            page.getResourceManager()
                    .removeResource(aggregateSecurityEventListenerAddress.getLastPairValue())
                    .confirmAndDismissReloadRequiredMessage();

            assertFalse(page.getResourceManager()
                    .isResourcePresent(aggregateSecurityEventListenerAddress.getLastPairValue()));

            new ResourceVerifier(aggregateSecurityEventListenerAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(aggregateSecurityEventListenerAddress);
            ops.removeIfExists(eventListener1);
            ops.removeIfExists(eventListener2);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Aggregate Security Event Listener instance in model and try to edit its
     * security-event-listeners attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editSecurityEventListeners() throws Exception {
        final Address eventListener1 = elyOps.getElytronAddress(FILE_AUDIT_LOG, randomAlphabetic(7)),
                eventListener2 = elyOps.getElytronAddress(FILE_AUDIT_LOG, randomAlphabetic(7)),
                eventListener3 = elyOps.getElytronAddress(FILE_AUDIT_LOG, randomAlphabetic(7)),
                eventListener4 = elyOps.getElytronAddress(FILE_AUDIT_LOG, randomAlphabetic(7)),
                aggregateSecurityEventListenerAddress = elyOps.getElytronAddress(AGGREGATE_SECURITY_EVENT_LISTENER,
                        randomAlphabetic(7));
        try {
            createFileAuditLog(eventListener1);
            createFileAuditLog(eventListener2);
            createFileAuditLog(eventListener3);
            createFileAuditLog(eventListener4);

            createAggregateSecurityEventListener(aggregateSecurityEventListenerAddress, eventListener1, eventListener2);

            page.navigateToApplication()
                    .switchSubTab(AGGREGATE_SECURITY_EVENT_LISTENER_LABEL);

            page.getResourceManager().selectByName(aggregateSecurityEventListenerAddress.getLastPairValue());

            new ConfigChecker.Builder(client, aggregateSecurityEventListenerAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, SECURITY_EVENT_LISTENERS, String.join("\n",
                            new String[]{eventListener3.getLastPairValue(), eventListener4.getLastPairValue()}))
                    .verifyFormSaved()
                    .verifyAttribute(SECURITY_EVENT_LISTENERS, new ModelNodeGenerator.ModelNodeListBuilder()
                            .addNode(new ModelNode(eventListener3.getLastPairValue()))
                            .addNode(new ModelNode(eventListener4.getLastPairValue()))
                            .build());
        } finally {
            ops.removeIfExists(aggregateSecurityEventListenerAddress);
            ops.removeIfExists(eventListener1);
            ops.removeIfExists(eventListener2);
            ops.removeIfExists(eventListener3);
            ops.removeIfExists(eventListener4);
            adminOps.reloadIfRequired();
        }
    }

    private void createFileAuditLog(Address address) throws IOException {
        ops.add(address, Values.of(PATH, randomAlphabetic(7))).assertSuccess();
    }

    private void createAggregateSecurityEventListener(Address address, Address... eventListeners) throws IOException {
        ops.add(address, Values.of(SECURITY_EVENT_LISTENERS, new ModelNodeGenerator.ModelNodeListBuilder()
                .addAll(Arrays.stream(eventListeners)
                        .map(Address::getLastPairValue)
                        .toArray(String[]::new))
                .build()))
                .assertSuccess();
    }
}
