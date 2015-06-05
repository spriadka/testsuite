package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.fragment.config.web.modcluster.ModClusterConfigArea;
import org.jboss.hal.testsuite.page.ConfigPage;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@Location("#modcluster")
public class ModClusterPage extends ConfigPage {

    private static final By CONFIG_AREA = By.className(PropUtils.get("configarea.class"));

    public ModClusterConfigArea getConfig(){
        return Graphene.createPageFragment(ModClusterConfigArea.class, getContentRoot().findElement(CONFIG_AREA));
    }
}
