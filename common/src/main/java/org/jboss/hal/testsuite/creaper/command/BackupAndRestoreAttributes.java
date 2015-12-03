package org.jboss.hal.testsuite.creaper.command;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.ReadResourceOption;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 27.11.15.
 */
public class BackupAndRestoreAttributes {

    private final Address address;
    private final Map<String, String> dependencies;
    private final Set<String> excluded;

    private ModelNode backup;

    private BackupAndRestoreAttributes(Builder builder) {
        this.address = builder.address;
        this.dependencies = builder.dependencies;
        this.excluded = builder.excluded;
    }

    private final OnlineCommand backupPart = new OnlineCommand() {

        @Override
        public void apply(OnlineCommandContext ctx) throws Exception {
            if (BackupAndRestoreAttributes.this.backup != null) {
                throw new CommandFailedException("Backup has been already made!");
            }
            Operations ops = new Operations(ctx.client);

            BackupAndRestoreAttributes.this.backup = ops.readResource(address, ReadResourceOption.INCLUDE_DEFAULTS, ReadResourceOption.ATTRIBUTES_ONLY).value();
        }
    };

    private final OnlineCommand restorePart = new OnlineCommand() {

        //<dependency which needs to be processed before dependent attribute is added to batch, dependent attributes>
        private final Map<String, Set<Property>> waitingForDependency = new HashMap<>(); //waiting queue
        private final Set<String> processedAttributes = new HashSet<>(); //attributes already in batch
        private final Batch batch = new Batch();

        private void putToWaitingQueue(String dependency, Property dependentAttribute) {
            if (waitingForDependency.containsKey(dependency)) {
                waitingForDependency.get(dependency).add(dependentAttribute);
            } else {
                waitingForDependency.put(dependency, new HashSet<>());
            }
        }

        private void addToBatch(Property node) {
            batch.writeAttribute(address,
                    node.getName(),
                    node.getValue()); //add dependent attribute to batch
            processedAttributes.add(node.getName()); //add to processed attributes
        }

        private void addAllToBatch(Collection<Property> nodes) {
            nodes.forEach(this::addToBatch);
        }

        private void addAllWaitingDependenciesToBatch(Property dependency) {
            String dependencyName = dependency.getName();
            if (waitingForDependency.containsKey(dependencyName)) { //attribute which was added to batch is dependency for some other attribute
                addAllToBatch(waitingForDependency.get(dependencyName)); //add dependent attributes to batch
                waitingForDependency.remove(dependencyName);
            }
        }

        @Override
        public void apply(OnlineCommandContext ctx) throws Exception {
            if (BackupAndRestoreAttributes.this.backup == null) {
                throw new CommandFailedException("There is no backup to be restored!");
            }

            for (Property processingAttribute : BackupAndRestoreAttributes.this.backup.asPropertyList()) {
                String name = processingAttribute.getName();

                if (excluded == null || !excluded.contains(name)) { //attribute is not excluded
                    if (dependencies != null && dependencies.containsKey(name)) { //processed element depends on something
                        if (processedAttributes.contains(dependencies.get(name))) { //dependency is already processed in batch
                            addToBatch(processingAttribute);
                        } else {
                            putToWaitingQueue(dependencies.get(name), processingAttribute);
                        }
                    } else {
                        addToBatch(processingAttribute);
                        addAllWaitingDependenciesToBatch(processingAttribute);
                    }
                }
            }

            Operations ops = new Operations(ctx.client);
            ops.batch(batch);
        }

    };

    public static final class Builder {

        private Address address;
        private Map<String, String> dependencies;
        private Set<String> excluded;

        public Builder(Address address) {
            this.address = address;
        }

        /**
         * Add dependency
         */
        public Builder dependency(String attribute, String dependsOn) {
            if (dependencies == null && dependsOn != null) {
                dependencies = new HashMap<>();
            }
            if (dependencies != null) {
                dependencies.put(attribute, dependsOn);
            }
            return this;
        }

        /**
         * Add attribute which will be excluded from restoring
         */
        public Builder excluded(String attribute) {
            if (excluded == null && attribute != null) {
                excluded = new HashSet<>();
            }
            if (attribute != null) {
                excluded.add(attribute);
            }
            return this;
        }

        public BackupAndRestoreAttributes build() {
            return new BackupAndRestoreAttributes(this);
        }

    }

    public OnlineCommand backup() {
        return backupPart;
    }

    public OnlineCommand restore() {
        return restorePart;
    }

}
