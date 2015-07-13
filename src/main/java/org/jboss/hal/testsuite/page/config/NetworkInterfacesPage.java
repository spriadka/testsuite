package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.fragment.config.interfaces.NetworkInterfaceContentFragment;
import org.jboss.hal.testsuite.page.ConfigPage;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@Location("#profile/interfaces")
public class NetworkInterfacesPage extends ConfigPage {

    private static final By CONTENT = By.id(PropUtils.get("page.content.area.id"));

    public NetworkInterfaceContentFragment getContent(){
        return Graphene.createPageFragment(NetworkInterfaceContentFragment.class, getContentRoot().findElement(CONTENT));
    }
}
