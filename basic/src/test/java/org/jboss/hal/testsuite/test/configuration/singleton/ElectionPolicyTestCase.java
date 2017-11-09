package org.jboss.hal.testsuite.test.configuration.singleton;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindowWithAdvancedSelectBoxOptions;
import org.jboss.hal.testsuite.page.config.SingletonSubsystemPage;
import org.jboss.hal.testsuite.util.AvailablePortFinder;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class ElectionPolicyTestCase {

    @Drone
    private WebDriver browser;

    @Page
    private SingletonSubsystemPage page;

    private static final String
            SINGLETON_POLICY_RESOURCE = "s-pol_" + RandomStringUtils.randomAlphanumeric(5),
            SINGLETON_POLICY_EDIT_RESOURCE = "s-pol-edit_" + RandomStringUtils.randomAlphanumeric(5),
            CACHE_CONTAINER_RESOURCE = "cache-container-singleton_" + RandomStringUtils.randomAlphanumeric(5),
            CACHE_CONTAINER = "cache-container",
            SINGLETON_POLICY = "singleton-policy",
            ELECTION_POLICY = "election-policy",
            NAME_PREFERENCES = "name-preferences",
            SOCKET_BINDING_PREFERENCES = "socket-binding-preferences",
            SIMPLE = "simple",
            RANDOM = "random";

    private static final Address
            SINGLETON_SUBSYSTEM_ADDRESS = Address.subsystem("singleton"),
            INFINISPAN_SUBSYSTEM_ADDRESS = Address.subsystem("infinispan"),
            SINGLETON_POLICY_EDIT_ADDRESS = SINGLETON_SUBSYSTEM_ADDRESS.and("singleton-policy", SINGLETON_POLICY_EDIT_RESOURCE),
            SINGLETON_POLICY_ADDRESS = SINGLETON_SUBSYSTEM_ADDRESS.and("singleton-policy", SINGLETON_POLICY_RESOURCE),
            CACHE_CONTAINER_RESOURCE_ADDRESS = INFINISPAN_SUBSYSTEM_ADDRESS.and("cache-container", CACHE_CONTAINER_RESOURCE);

    private static final OnlineManagementClient client = ConfigUtils.isDomain() ?
            ManagementClientProvider.withProfile("full-ha") :
            ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);
    private static final Operations operations = new Operations(client);

    private static final SingletonSubsystemOperations singletonOperations = new SingletonSubsystemOperations(client);

    private static SocketBindingResources socketBindingResources;

    @Before
    public void before() {
        page.navigate();
    }

    @BeforeClass
    public static void beforeClass() throws IOException, CommandFailedException {
        //Prepare resources required by test cases
        socketBindingResources = new SocketBindingResources(client, 3, "election-policy_");
        singletonOperations.prepareCacheContainer(CACHE_CONTAINER_RESOURCE_ADDRESS);
        //Add singleton policy without election policy
        operations.add(SINGLETON_POLICY_ADDRESS, Values.of(CACHE_CONTAINER, CACHE_CONTAINER_RESOURCE)).assertSuccess();
        //Add singleton policy with defined election policy
        operations.batch(new Batch()
                .add(SINGLETON_POLICY_EDIT_ADDRESS, Values.of(CACHE_CONTAINER, CACHE_CONTAINER_RESOURCE))
                .add(SINGLETON_POLICY_EDIT_ADDRESS.and(ELECTION_POLICY, SIMPLE))).assertSuccess();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        try {
            operations.removeIfExists(SINGLETON_POLICY_ADDRESS);
            operations.removeIfExists(SINGLETON_POLICY_EDIT_ADDRESS);
            singletonOperations.removeCacheContainer(CACHE_CONTAINER_RESOURCE_ADDRESS);
            socketBindingResources.clean();
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void addRandomElectionPolicyInUI() throws Exception {
        page.treeNavigation()
                .step(SINGLETON_POLICY)
                .step(SINGLETON_POLICY_RESOURCE)
                .step(ELECTION_POLICY)
                .navigateToTreeItem().clickLabel();

        WizardWindowWithAdvancedSelectBoxOptions window = page.getResourceManager()
                .addResource(WizardWindowWithAdvancedSelectBoxOptions.class);

        window.pick(RANDOM)
                .clickContinue()
                .finish();

        new ResourceVerifier(SINGLETON_POLICY_ADDRESS.and(ELECTION_POLICY, RANDOM), client)
                .verifyExists("This probably fails because of https://issues.jboss.org/browse/HAL-1256");
    }

    @Test
    public void editNamePreferences() throws Exception {
        operations.undefineAttribute(SINGLETON_POLICY_EDIT_ADDRESS.and(ELECTION_POLICY, SIMPLE), SOCKET_BINDING_PREFERENCES).assertSuccess();

        page.treeNavigation()
                .step(SINGLETON_POLICY)
                .step(SINGLETON_POLICY_EDIT_RESOURCE)
                .step(ELECTION_POLICY)
                .step(SIMPLE)
                .navigateToTreeItem().clickLabel();

        new ConfigChecker.Builder(client, SINGLETON_POLICY_EDIT_ADDRESS.and(ELECTION_POLICY, SIMPLE))
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, NAME_PREFERENCES, "foo\nbar\nqiz")
                .verifyFormSaved()
                .verifyAttribute(NAME_PREFERENCES, new ModelNode().add("foo").add("bar").add("qiz"));
    }

    @Test
    public void editSocketBindingPreferences() throws Exception {
        operations.undefineAttribute(SINGLETON_POLICY_EDIT_ADDRESS.and(ELECTION_POLICY, SIMPLE), NAME_PREFERENCES).assertSuccess();

        page.treeNavigation()
                .step(SINGLETON_POLICY)
                .step(SINGLETON_POLICY_EDIT_RESOURCE)
                .step(ELECTION_POLICY)
                .step(SIMPLE)
                .navigateToTreeItem().clickLabel();

        new ConfigChecker.Builder(client, SINGLETON_POLICY_EDIT_ADDRESS.and(ELECTION_POLICY, SIMPLE))
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, SOCKET_BINDING_PREFERENCES, socketBindingResources.getSocketBindingNamesSeparatedByNewline())
                .verifyFormSaved()
                .verifyAttribute(SOCKET_BINDING_PREFERENCES, socketBindingResources.getSocketBindingsAsModelNodeList());
    }

    /**
     * Class used for allocating remote-destination-outbound-socket-binding resources in this test case.
     */
    private static final class SocketBindingResources {

        private static final String SOCKET_BINDING_TYPE = "remote-destination-outbound-socket-binding";

        private final Operations operations;
        private final int count;
        private final String prefix;
        private final List<String> socketBindingsNames;
        private final Address socketBindingGroupAddress;

        /**
         * Creates an instance. Socket bindings are created during instantiating.
         * @param client used for executing operations
         * @param count count of socket bindings to create
         * @param prefix prefix used for name of each socket binding
         * @throws CommandFailedException
         * @throws IOException
         */
        SocketBindingResources(OnlineManagementClient client, int count, String prefix) throws CommandFailedException, IOException {
            this.operations = new Operations(client);
            this.count = count;
            this.prefix = prefix;

            String contextSocketBindingGroup = client.options().isDomain ? "full-ha-sockets" : "standard-sockets";
            this.socketBindingGroupAddress = Address.of("socket-binding-group", contextSocketBindingGroup);

            this.socketBindingsNames = createSocketBindings();
        }

        private String generateName() {
            return prefix + RandomStringUtils.randomAlphanumeric(5);
        }

        private List<String> createSocketBindings() throws IOException {
            final List<String> socketBindingNames = new LinkedList<>();
            for (int i = 0; i < count; i++) {
                String name = generateName();
                Address remoteSocketBindingAddress = this.socketBindingGroupAddress.and(SOCKET_BINDING_TYPE, name);
                operations.add(remoteSocketBindingAddress,
                        Values.of("host", "el-policy-host_" + RandomStringUtils.randomAlphanumeric(5))
                            .and("port", AvailablePortFinder.getNextAvailableNonPrivilegedPort())).assertSuccess();
                socketBindingNames.add(name);
            }
            return socketBindingNames;
        }

        /**
         * Get names of created socket bindings separated by newline
         * @return names of created socket bindings separated by newline
         */
        public String getSocketBindingNamesSeparatedByNewline() {
            return socketBindingsNames.stream().collect(Collectors.joining("\n"));
        }

        /**
         * Get socket bindings as {@link ModelNode} list
         * @return socket bindings as {@link ModelNode} list
         */
        public ModelNode getSocketBindingsAsModelNodeList() {
            final ModelNode node = new ModelNode();
            socketBindingsNames.forEach(node::add);
            return node;
        }

        /**
         * Removes all created socket bindings
         * @throws IOException
         */
        public void clean() throws IOException {
            for (String socketBinding : socketBindingsNames) {
                operations.remove(this.socketBindingGroupAddress.and(SOCKET_BINDING_TYPE, socketBinding)).assertSuccess();
            }
        }
    }


}
