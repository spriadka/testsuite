package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.fragment.config.datasource.DatasourceConfigArea;
import org.jboss.hal.testsuite.fragment.config.datasource.DatasourceWizard;
import org.jboss.hal.testsuite.util.Console;

/**
 * Created by jcechace on 22/02/14.
 */

@Location("#profiles/ds-finder")
public class DatasourcesPage extends ConfigurationPage {
    @Override
    public DatasourceConfigArea getConfig() {
        return getConfig(DatasourceConfigArea.class);
    }

    public DatasourceWizard addResource() {
        return addResource(DatasourceWizard.class);
    }

    public void switchToXA() {
        select("XA");
        Console.withBrowser(browser).waitUntilLoaded();
    }

}
