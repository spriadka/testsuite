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

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 27.11.15.
 */
public class BackupAndRestoreAttributes {

    private final Address address;

    private ModelNode backup;

    public BackupAndRestoreAttributes(Address address) {
        this.address = address;
    }

    private final OnlineCommand backupPart = new OnlineCommand() {

        @Override
        public void apply(OnlineCommandContext ctx) throws Exception {
            if (BackupAndRestoreAttributes.this.backup != null) {
                throw new CommandFailedException("Backup has been already made!");
            }
            Operations ops = new Operations(ctx.client);

            backup = ops.readResource(address, ReadResourceOption.INCLUDE_DEFAULTS, ReadResourceOption.ATTRIBUTES_ONLY).value();
        }
    };

    private final OnlineCommand restorePart = new OnlineCommand() {

        @Override
        public void apply(OnlineCommandContext ctx) throws Exception {
            if (BackupAndRestoreAttributes.this.backup != null) {
                throw new CommandFailedException("There is no backup to be restored!");
            }
            Operations ops = new Operations(ctx.client);
            Batch batch = new Batch();
            for (Property property : backup.asPropertyList()) {
                batch.writeAttribute(address, property.getName(), property.getValue());
            }
            ops.batch(batch);
        }

    };

    public OnlineCommand backup() {
        return backupPart;
    }

    public OnlineCommand restore() {
        return restorePart;
    }

}
