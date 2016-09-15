package org.jboss.hal.testsuite.fragment.runtime.configurationchanges.table;

import org.jboss.hal.testsuite.fragment.shared.table.GenericResourceTableFragment;
import org.jboss.hal.testsuite.util.configurationchanges.ConfigurationChange;
import org.jboss.hal.testsuite.util.configurationchanges.ConfigurationChangesProvider;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstraction over UI table containing configuration changes
 *
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 9/12/16.
 */
public class ConfigurationChangesTableFragment extends GenericResourceTableFragment<ConfigurationChangeRowFragment> implements ConfigurationChangesProvider {

    public ConfigurationChangeRowFragment getLastChange() {
        return getAllRows().get(0);
    }

    public List<ConfigurationChange> getAllConfigurationChanges() {
        return getAllRows().stream()
                .map((ConfigurationChangeRowFragment row) -> (ConfigurationChange) row)
                .collect(Collectors.toList());
    }

    public State getState() {
        return new State(getAllRows(), getAllConfigurationChanges());
    }

    /**
     * Current state of {@link ConfigurationChangesTableFragment}
     */
    public static final class State implements ConfigurationChangesProvider {

        private List<ConfigurationChangeRowFragment> rows;
        private List<ConfigurationChange> configurationChanges;

        State(List<ConfigurationChangeRowFragment> rows, List<ConfigurationChange> configurationChanges) {
            this.rows = rows;
            this.configurationChanges = configurationChanges;
        }

        public List<ConfigurationChangeRowFragment> getAllChangesRow() {
            return rows;
        }

        @Override
        public List<ConfigurationChange> getAllConfigurationChanges() throws Exception {
            return configurationChanges;
        }
    }
}
