package org.jboss.hal.testsuite.page.config.elytron;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.hal.testsuite.fragment.config.elytron.permissionmapper.PermissionsTableFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.jboss.hal.testsuite.finder.FinderNames.SETTINGS;
import static org.jboss.hal.testsuite.page.config.elytron.ElytronPageConstants.ELYTRON_SUBSYTEM_LABEL;

public class MapperDecoderPage extends AbstractElytronConfigPage<MapperDecoderPage> {

    private static final String
        MAPPER_DECODER = "Mapper / Decoder",
        ROLE_MAPPER = "Role Mapper",
        PERMISSION_MAPPER = "Permission Mapper",
        DECODER = "Decoder";

    @Override
    public MapperDecoderPage navigateToApplication() {
        return navigateToRoleMapper();
    }

    public MapperDecoderPage navigateToRoleMapper () {
        return navigateToTab(ROLE_MAPPER);
    }

    public MapperDecoderPage navigateToPermissionMapper () {
        return navigateToTab(PERMISSION_MAPPER);
    }

    public MapperDecoderPage navigateToDecoder () {
        return navigateToTab(DECODER);
    }

    private MapperDecoderPage navigateToTab(String tabIdentifier) {
        getSubsystemNavigation(ELYTRON_SUBSYTEM_LABEL).step(SETTINGS, MAPPER_DECODER).openApplication(30);
        switchTab(tabIdentifier);
        return this;
    }

    public PermissionsTableFragment getPermissionsTable() {
        WebElement windowPanel = browser.findElement(By.className("default-window-content"));
        return Graphene.createPageFragment(PermissionsTableFragment.class, windowPanel);
    }

}
