package org.jboss.hal.testsuite.fragment.config.interfaces;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public class NetworkInterfaceContentFragment extends ConfigFragment {

    public static final String NIC = "nic";
    private static final String NIC_MATCH = "nicMatch";
    private static final String LOOPBACK_ADDRESS = "loopbackAddress";

    public NetworkInterfaceWizard addInterface(){
        return getResourceManager().addResource(NetworkInterfaceWizard.class);
    }

    public void removeInterface(String interfaceName){
        getResourceManager().removeResourceAndConfirm(interfaceName);
    }

    public void editAndSave(String interfaceName, String attribute, String value){
        getResourceManager().selectByName(interfaceName);
        Editor editor = edit();

        root.sendKeys(Keys.PAGE_DOWN);
        editor.text(attribute, value);
        WebElement saveButton = getButton(PropUtils.get("configarea.save.button.label"));
        Graphene.waitGui().until().element(saveButton).is().visible();
        save();
    }

    public void editNicAndSave(String interfaceName, String value){
        editAndSave(interfaceName, NIC, value);
    }
    public void editNicMatchAndSave(String interfaceName, String value){
        editAndSave(interfaceName, NIC_MATCH, value);
    }
    public void editLoopbackAddressAndSave(String interfaceName, String value){
        editAndSave(interfaceName, LOOPBACK_ADDRESS, value);
    }
}
